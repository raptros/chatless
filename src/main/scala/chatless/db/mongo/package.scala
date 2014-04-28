package chatless.db

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
  import chatless.db.{DbError, TopicDAO, NoSuchTopic, DeserializationErrors}

  val ApV = Applicative[({type λ[α]=ValidationNel[String, α]})#λ]

  implicit class ValidatedDBO(dbo: DBObject) {

    def extractKey[A: NotNothing: Manifest](key: String) = if (dbo.containsField(key)) {
      dbo.getAs[A](key) \/> s"could not cast key $key as ${implicitly[Manifest[A]].toString()}"
    } else {
      s"dbo does not contain key $key".left[A]
    }

    def validateKey[A: NotNothing: Manifest](key: String) = extractKey[A](key).validation

    def validateKeyNel[A: NotNothing: Manifest](key: String) = validateKey[A](key).toValidationNel
  }

  trait FilteredRetryEitherStep2[A, B] {
    def p: A => Boolean
    def orig: A \/ B

    def thenTry[AA >: A, BB >: B](alt: => AA \/ BB): AA \/ BB = orig match {
      case -\/(a) if p(a) => alt
      case _ => orig
    }
  }

  implicit class FilteredRetryEither[A, B](either: A \/ B) {
    def whenLeft(f: A => Boolean) = new FilteredRetryEitherStep2[A, B] {
      val p = f
      val orig = either
    }
  }

  //fields
  val idField: String = "_id"

  val serverField: String = "server"

  val userField: String = "user"

  val bannerField: String = "banner"

  val infoField: String = "info"

}
