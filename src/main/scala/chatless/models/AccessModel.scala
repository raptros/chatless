package chatless.models

import com.mongodb.casbah.Imports._

import scala.reflect.runtime.universe._
import com.mongodb.casbah.commons.NotNothing

import scalaz._
import scalaz.syntax.std.option._

trait AccessModel {
  type ModelField[+A] <: TypedField[A]

  val dbo: MongoDBObject

  def get[A](field: ModelField[A]): ValidationNel[String, A] = field extractFrom dbo
}
