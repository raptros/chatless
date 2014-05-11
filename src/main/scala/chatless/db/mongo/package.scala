package chatless.db

import com.mongodb.casbah.commons

package object mongo {
  import com.mongodb.casbah.Imports._
  import chatless.model.{TopicInit, UserCoordinate, Topic, TopicCoordinate}
  import scalaz._
  import scalaz.syntax.std.option._
  import scalaz.syntax.validation._
  import scalaz.syntax.id._

  import com.osinka.subset._

  import argonaut._
  import Argonaut._
  import com.mongodb.casbah.commons.NotNothing
  import chatless.db.{DbError, TopicDAO, DeserializationErrors}

  object Fields extends Enumeration {

    type Field = Value
    //note: we basically never use _id in documents. this is intentional
    val _id, id, server, user, timestamp, message, topic, body, joined, poster, pos, banner, info, counter = Value
  }

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