package chatless.db

import chatless.{UserId, TopicId}
import argonaut._
import Argonaut._
import chatless.CustomCodecs._

import scala.reflect.runtime.universe._

sealed abstract class OpRes {
  def asJson:Json = jEmptyObject
}
case class ResUser(uid:UserId) extends OpRes {
  override def asJson:Json = ("res" := "user") ->: ("uid" := uid) ->: super.asJson
}
case class ResTopic(tid:TopicId) extends OpRes {
  override def asJson:Json = ("res" := "topic") ->: ("tid" := tid) ->: super.asJson
}

sealed abstract class OpSpec {
  def asJson:Json = jEmptyObject
}
sealed abstract class GetSpec extends OpSpec {
  override def asJson = ("op" := "get") ->: super.asJson
}
case class GetFields(field:String*) extends GetSpec {
  override def asJson = ("fields" := field.toList) ->: super.asJson
}
case object GetAll extends GetSpec {
  def apply(a:Any) = this
  override def asJson = ("allfields" := true) ->: super.asJson
}

sealed abstract class UpdateSpec[+A:TaggedAndEncodable] extends OpSpec {
  val value:A
  val field:String
  override def asJson = ("op" := "update") ->:
    ("field" := field) ->:
    ("value" := value) ->:
    ("type" := typeTag[A].tpe.toString) ->:
    super.asJson
}
case class ReplaceField[A:TaggedAndEncodable](field:String, value:A) extends UpdateSpec {
  override def asJson = ("spec" := "replace") ->: super.asJson
}
case class AppendToList[A:TaggedAndEncodable](field:String, value:A) extends UpdateSpec {
  override def asJson = ("spec" := "append") ->: super.asJson
}
case class DeleteFromList[A:TaggedAndEncodable](field:String, value:A) extends UpdateSpec {
  override def asJson = ("spec" := "delete") ->: super.asJson
}

case class Operation(cid:UserId, res:OpRes, spec:OpSpec)







