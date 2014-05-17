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
  }

  trait FilteredRetryEitherStep2[A, B] {
    def p: A => Boolean
    def orig: A \/ B

    def thenTry[AA >: A, BB >: B](alt: => AA \/ BB): AA \/ BB = orig match {
      case -\/(a) if p(a) => alt
      case _ => orig
    }
  }
}
