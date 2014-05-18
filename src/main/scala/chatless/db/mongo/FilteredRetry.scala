package chatless.db.mongo

import scalaz._

object FilteredRetry {
  /** enables applicative fun */
  val ApV = Applicative[({type λ[α]=ValidationNel[String, α]})#λ]

  /** enables a conditional retry syntax for scalaz disjunctions - disj whenLeft (predicate) thenTry (disj of same type)
    */
  implicit class FilteredRetryEither[A, B](either: A \/ B) {
    def whenLeft(f: A => Boolean) = new FilteredRetryEitherStep2[A, B] {
      val p = f
      val orig = either
    }

    /** attemptLeft takes a partial function, which will be tested against the left side of the original disjunction.
      * if possible, the partial function will be run on that left side, and the result will be passed into the
      * `thenTry` block. if not, the original disjunction will be returned.
      */
    def attemptLeft[X](part: PartialFunction[A, X]) = new FilteredRetryEitherStep2V2[A, B, X] {
      val f = part
      val orig = either
    }
  }

  trait FilteredRetryEitherStep2V2[A, B, X] {
    def f: PartialFunction[A, X]
    def orig: A \/ B
    def thenTry[AA >: A, BB >: B](alt: X => AA \/ BB): AA \/ BB = orig match {
      case -\/(a) if f isDefinedAt a => alt(f(a))
      case _ => orig
    }
  }

  trait FilteredRetryEitherStep2[A, B] { step2 =>
    def p: A => Boolean
    def orig: A \/ B

    def thenTry[AA >: A, BB >: B](alt: => AA \/ BB): AA \/ BB = orig match {
      case -\/(a) if p(a) => alt
      case _ => orig
    }

    def firstExtract[X](f: A => X) = new FilteredRetryEitherStep3[A, B, X] {
      val p = step2.p
      val x = f
      val orig = step2.orig
    }
  }

  trait FilteredRetryEitherStep3[A, B, X] {
    def p: A => Boolean
    def x: A => X
    def orig: A \/ B

    def thenTry[AA >: A, BB >: B](alt: X => AA \/ BB): AA \/ BB = orig match {
      case -\/(a) if p(a) => alt(x(a))
      case _ => orig
    }
  }
}
