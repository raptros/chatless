package chatless.db

import com.mongodb.casbah.commons.NotNothing

package object daos {
  import chatless._
  import chatless.models._
  import com.mongodb.casbah.Imports._
  import scalaz._
  import scalaz.syntax.validation._
  import scalaz.syntax.std.option._
  import shapeless._
  import shapeless.Typeable._

  trait DAO {
    type ID
    type Model

    def extractField[A:NotNothing:Manifest](field: String, obj: MongoDBObject): ValidationNel[String, A] =
      obj.getAs[A](field).toSuccess(s"could not get $field from $obj").toValidationNel

    def get(id: ID): Option[Model]
  }

  trait UserDAO extends DAO {
    type ID = UserId
    type Model = UserM
  }

  trait TopicDAO extends DAO {
    type ID = TopicId
    type Model = TopicM
  }
}
