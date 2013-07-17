package chatless.db

import chatless.{UserId, TopicId, RequestId}
import argonaut._
import Argonaut._

import scala.reflect.runtime.universe._
import scalaz.syntax.bind._

sealed abstract class ValueContainer {
  type A
  protected implicit def ej : EncodeJson[A]
  def contained:A
  def asJson:Json = ("contained" := contained) ->: jEmptyObject
}

object ValueContainer {
  implicit def VCEncodeJ:EncodeJson[ValueContainer] = EncodeJson { vc:ValueContainer => vc.asJson }

  implicit def JDecodeVC:DecodeJson[ValueContainer] = DecodeJson { c =>
    (c --\ "type").as[String] flatMap {
      case "Boolean" => (c --\ "contained").as[Boolean] map { BooleanVC }
      case "String" => (c --\ "contained").as[String] map { StringVC }
      case "Json" => (c --\ "contained").as[Json] map { JsonVC }
    }
  }
}

case class BooleanVC(contained:Boolean) extends ValueContainer {
  type A = Boolean
  implicit val ej = BooleanEncodeJson
  override def asJson =  ("type" := "Boolean") ->: super.asJson
}

case class StringVC(contained:String) extends ValueContainer {
  type A = String
  implicit val ej = StringEncodeJson
  override def asJson = ("type" := jString("String")) ->: super.asJson
}

case class JsonVC(contained:Json) extends ValueContainer {
  type A = Json
  implicit val ej = JsonEncodeJson
  override def asJson = ("type" := "Json") ->: super.asJson
}

sealed abstract class OpRes {
  def asJson:Json = jEmptyObject
}

object OpRes {
  implicit def OpResEncodeJ:EncodeJson[OpRes] = EncodeJson { _.asJson }

  implicit def JDecodeOpRes:DecodeJson[OpRes] = DecodeJson { c =>
    (c --\ "res").as[String] flatMap {
      case "me" => okResult { ResMe }
      case "user" => (c --\ "uid").as[String] map { ResUser }
      case "topic" => (c --\ "tid").as[String] map { ResTopic }
      case "req" => (c --\ "rid").as[String] map { ResRequest }
      case "mreqs" => okResult { ResMeReqs }
      case "ureqs" => (c --\ "uid").as[String] map { ResUserReqs }
      case "treqs" => (c --\ "tid").as[String] map { ResTopicReqs }
      case _ => DecodeResult.fail("not a valid resource spec", c.history)
    }
  }
}

case object ResMe extends OpRes {
  override def asJson:Json = ("res" := "me") ->: super.asJson
}

case class ResUser(uid:UserId) extends OpRes {
  override def asJson:Json = ("res" := "user") ->: ("uid" := uid) ->: super.asJson
}

case class ResTopic(tid:TopicId) extends OpRes {
  override def asJson:Json = ("res" := "topic") ->: ("tid" := tid) ->: super.asJson
}

case class ResRequest(rid:RequestId) extends OpRes {
  override def asJson:Json = ("res" := "req") ->: ("rid" := rid) ->: super.asJson
}

case object ResMeReqs extends OpRes {
  override def asJson:Json = ("res" := "mreqs") ->: super.asJson
}
case class ResUserReqs(uid:UserId) extends OpRes {
  override def asJson:Json = ("res" := "ureqs") ->: ("uid" := uid) ->: super.asJson
}

case class ResTopicReqs(tid:TopicId) extends OpRes {
  override def asJson:Json = ("res" := "treqs") ->: ("tid" := tid) ->: super.asJson
}

sealed abstract class OpSpec {
  def asJson:Json = jEmptyObject
}

object OpSpec {
  implicit def OpSpecEncodeJ:EncodeJson[OpSpec] = EncodeJson { _.asJson }

  implicit def jdos:DecodeJson[OpSpec] = DecodeJson { c =>
    (c --\ "op").as[String] flatMap {
      case "get" => (c --\ "spec").as[String] flatMap {
        case "all" => okResult(GetAll)
        case "fields" => (c --\ "fields").as[List[String]] map { fields => GetFields(fields: _*) }
        case "contains" => jdecode2L { GetListContains } ("field", "value") decode c
      }
      case "update" => (c --\ "spec").as[String] map {
        case "replace" => jdecode2L { ReplaceField } ("field", "value")
        case "append" => jdecode2L { AppendToList } ("field", "value")
        case "delete" => jdecode2L { DeleteFromList } ("field", "value")
      } flatMap { _ decode c }
      case "create" => (c --\ "value").as[ValueContainer] map { Create.apply _ }
      case _ => failResult(s"no such op", c.history)
    }
  }
}

sealed abstract class GetSpec extends OpSpec {
  override def asJson = ("op" := "get") ->: super.asJson
}

case class GetFields(field:String*) extends GetSpec {
  override def asJson = ("spec" := "fields") ->: ("fields" := field.toList) ->: super.asJson
}

case class GetListContains(field:String, value:ValueContainer) extends GetSpec {
  override def asJson = ("spec" := "contains") ->: ("field" := field) ->: ("value" := value) ->: super.asJson
}

case object GetAll extends GetSpec {
  def apply(a:Any) = this
  override def asJson = ("spec" := "all") ->: super.asJson
}

sealed abstract class UpdateSpec extends OpSpec {
  val value:ValueContainer
  val field:String
  override def asJson = ("op" := "update") ->:
    ("field" := field) ->:
    ("value" := value) ->:
    super.asJson
}

case class ReplaceField(field:String, value:ValueContainer) extends UpdateSpec {
  override def asJson = ("spec" := "replace") ->: super.asJson
}

case class AppendToList(field:String, value:ValueContainer) extends UpdateSpec {
  override def asJson = ("spec" := "append") ->: super.asJson
}

case class DeleteFromList(field:String, value:ValueContainer) extends UpdateSpec {
  override def asJson = ("spec" := "delete") ->: super.asJson
}

case class Create(value:ValueContainer) extends OpSpec {
  override def asJson = ("op" := "create") ->: ("value" := value) ->: super.asJson
}

case class Operation(cid:UserId, res:OpRes, spec:OpSpec)

object Operation {
  implicit def OperationEncodeJ:EncodeJson[Operation] = jencode3L { op:Operation =>
    (op.cid, op.res, op.spec)
  } ("cid", "res", "spec")

  implicit def JDecodeOperation:DecodeJson[Operation] = jdecode3L { Operation.apply } ("cid", "res", "spec")
}






