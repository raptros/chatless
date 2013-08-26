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
  import scala.reflect.runtime.universe._
  import argonaut._
  import Argonaut._

  implicit class ExtractableDBO(dbo: MongoDBObject) {
    def extract[A : NotNothing : Manifest : TypeTag](field: TypedField[A]): ValidationNel[String, A] = if (typeOf[A] =:= typeOf[TypedField[Json]]) {
      extract[String](field.name) flatMap { _.parse.validation.toValidationNel } map { _.asInstanceOf[A] }
    } else {
      extract[A](field.name)
    }

    def extract[A : NotNothing : Manifest : TypeTag](field: String): ValidationNel[String, A] =
      dbo.getAs[A](field).toSuccess(s"could not extract $field").toValidationNel
  }

  implicit class MongoCollectionOps(val collection: MongoCollection) {
    trait ResWithError {
      val res: Option[MongoDBObject]
      def withError[A](a: A): A \/ MongoDBObject = res \/> a
    }

    def lookup(q: DBObject) = new ResWithError {
      val res: Option[MongoDBObject] = (collection findOne q) map { wrapDBObj }
    }
  }

  type ValidModel[A] = StateError \/ A

  trait DAO {
    type ID
    type Model

    val collection: MongoCollection

    def get(id: ID): ValidModel[Model]
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
