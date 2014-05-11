package chatless.db.mongo

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

object ExtractorUtils {
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


}
