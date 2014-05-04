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
    val _id, id, server, user, banner, info = Value
  }

  /** enables applicative fun */
  val ApV = Applicative[({type λ[α]=ValidationNel[String, α]})#λ]

  /** wraps db objects and provides error-wrapping/validating methods for extracting values, */
  implicit class ValidatedDBO(dbo: DBObject) {
    import Fields._

    /** attempts to retrieve a key from the db object as a specified type.
      * @tparam A what type the key should be extracted as
      * @param key the key to pull from the db object
      * @return successfully, a value of type A, or a string describing the failure.
      */
    def extractKey[A: NotNothing: Manifest](key: Field) = if (dbo.containsField(key.toString)) {
      dbo.getAs[A](key.toString) \/> s"could not cast key $key as ${implicitly[Manifest[A]].toString()}"
    } else {
      s"dbo does not contain key $key".left[A]
    }

    /** does extractKey and converts to a [scalaz.Validation] */
    def validateKey[A: NotNothing: Manifest](key: Field) = extractKey[A](key).validation

    def validateKeyNel[A: NotNothing: Manifest](key: Field) = validateKey[A](key).toValidationNel
  }

  /** a way to build mongodb objects using Fields */
  def MDBO[B <: Any](pairs: (Fields.Field, B)*) = MongoDBObject( pairs.toList map { p => p._1.toString -> p._2 } )

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
