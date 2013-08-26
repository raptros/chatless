package chatless.models

import scala.reflect.runtime.universe._
import shapeless._
import spray.routing.PathMatcher
import spray.routing.PathMatcher._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.NotNothing
import scalaz._
import scalaz.syntax.std.option._

abstract class TypedField[A : TypeTag : NotNothing] {
  val name: String
  implicit val manifestA = typeTagToManifest(rootMirror, typeTag[A])

  def extractFrom(dbo: MongoDBObject): ValidationNel[String, A] =
    dbo.getAs[A](name).toSuccess(s"could not extract $name").toValidationNel
}

object TypedField {
  implicit def typedField2PathMatcher(tf: TypedField[_]): PathMatcher[HNil] = segmentStringToPathMatcher(tf.name)
}
