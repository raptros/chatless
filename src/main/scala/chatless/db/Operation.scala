package chatless.db

import chatless.{UserId, TopicId}
import argonaut._
import Argonaut._


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

sealed abstract class UpdateSpec[+A] extends OpSpec {
  override def asJson = ("op" := "update") ->: super.asJson
}
case class ReplaceField[A:EncodeJson](field:String, value:A) extends UpdateSpec {
  override def asJson = ("spec" := "replace") ->: ("field" := field) ->: ("value" := value) ->: super.asJson
}
case class AppendToList[A:EncodeJson](field:String, value:A) extends UpdateSpec {
  override def asJson = ("spec" := "append") ->: ("field" := field) ->: ("value" := value) ->: super.asJson
}
case class DeleteFromList[A:EncodeJson](field:String, value:A) extends UpdateSpec {
  override def asJson = ("spec" := "delete") ->: ("field" := field) ->: ("value" := value) ->: super.asJson
}

case class Operation(cid:UserId, res:OpRes, spec:OpSpec)







