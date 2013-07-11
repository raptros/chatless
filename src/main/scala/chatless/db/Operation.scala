package chatless.db

import chatless.{UserId, TopicId}
import argonaut._
import Argonaut._
import chatless.CustomCodecs._

import scala.reflect.runtime.universe._

abstract class ValueContainer[A:EncodeJson] {
  def contained:Json
  def asJson:Json = ("contained" := contained) ->: jEmptyObject
}

object ValueContainer {
  implicit def VCEncodeJ:EncodeJson[ValueContainer[_]] = EncodeJson { vc:ValueContainer[_] => vc.asJson }
}

case class BooleanVC(contained:Boolean) extends ValueContainer[Boolean] {
  override def asJson =  ("type" := "Boolean") ->: super.asJson
}

case class StringVC(contained:String) extends ValueContainer[String] {
  override def asJson = ("type" := "String") ->: super.asJson
}

sealed abstract class OpRes {
  def asJson:Json = jEmptyObject
}

case class ResUser(uid:UserId) extends OpRes {
  override def asJson:Json = ("res" := "user") ->: ("uid" := uid) ->: super.asJson
}

case class ResTopic(tid:TopicId) extends OpRes {
  override def asJson:Json = ("res" := "topic") ->: ("tid" := tid) ->: super.asJson
}

object OpRes {
  implicit def OpResEncodeJ:EncodeJson[OpRes] = EncodeJson { _.asJson }

  implicit def JDecodeOpRes:DecodeJson[OpRes] = DecodeJson { c => (c --\ "res").as[String] flatMap {
    case "user" => (c --\ "uid").as[String] map { uid => ResUser(uid) }
    case "topic" => (c --\ "tid").as[String] map { tid => ResTopic(tid) }
    case _ => DecodeResult.fail("not a valid resource spec", c.history)
  }}

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

sealed abstract class UpdateSpec extends OpSpec {
  val value:ValueContainer[_]
  val field:String
  override def asJson = ("op" := "update") ->:
    ("field" := field) ->:
    ("value" := value) ->:
    super.asJson
}

case class ReplaceField(field:String, value:ValueContainer[_]) extends UpdateSpec {
  override def asJson = ("spec" := "replace") ->: super.asJson
}

case class AppendToList(field:String, value:ValueContainer[_]) extends UpdateSpec {
  override def asJson = ("spec" := "append") ->: super.asJson
}

case class DeleteFromList(field:String, value:ValueContainer[_]) extends UpdateSpec {
  override def asJson = ("spec" := "delete") ->: super.asJson
}


object OpSpec {
  def JDecodeGetFields:DecodeJson[GetSpec] = DecodeJson { c => for {
    op <- (c --\ "op").as[String]
    fields <- (c --\ "fields").as[List[String]]
    res <- if (op == "get") okResult(GetFields(fields:_*)) else failResult("not a get all fields", c.history)
  } yield res }

  def JDecodeGetAll:DecodeJson[GetSpec] = DecodeJson { c => for {
    op <- (c --\ "op").as[String]
    allF <- (c --\ "allfields").as[Boolean]
    res <- if (op == "get" && allF) okResult(GetAll) else failResult("not a get all fields", c.history)
  } yield res }

  def JDecodeGetOp:DecodeJson[GetSpec] = JDecodeGetAll ||| JDecodeGetFields

  def JDecodeUpdateSpec1:DecodeJson[UpdateSpec] = DecodeJson { c => for {
    op <- (c --\ "op").as[String]
    spec <- (c --\ "spec").as[String]
    field <- (c --\ "field").as[String]
    value <- (c --\ "value ").as[ValueContainer[_]]
    res <- (op, spec) match {
      case ("update", "replace") => okResult(ReplaceField(field, value))
      case ("update", "replace") => okResult(ReplaceField(field, value))
    }
  } yield res }

}

case class Operation(cid:UserId, res:OpRes, spec:OpSpec)

object Operation {
  implicit def OperationEncodeJ:EncodeJson[Operation] = jencode3L { op:Operation =>
    (op.cid, op.res, op.spec)
  } ("cid", "res", "spec")

  implicit def JDecodeOperation:DecodeJson[Operation] = jdecode3L { Operation.apply } ("cid", "res", "spec")
}






