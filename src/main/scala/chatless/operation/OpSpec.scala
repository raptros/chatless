package chatless.operation

import chatless.{UserId, TopicId, RequestId}
import argonaut._
import Argonaut._

sealed abstract class OpSpec {
   def asJson:Json = jEmptyObject
}

object OpSpec {
  implicit def OpSpecEncodeJ:EncodeJson[OpSpec] = EncodeJson { _.asJson }

  implicit def jdos:DecodeJson[OpSpec] = DecodeJson { c =>
    (c --\ "op").as[String] flatMap {
      case "get" => (c --\ "spec").as[String] flatMap {
        case "all" => okResult(GetAll)
        case "field" => (c --\ "field").as[String] map { GetField }
        case "contains" => jdecode2L { GetListContains } ("field", "value") decode c
        case "relative" => jdecode4L { GetRelative }  ("forward", "baseId", "inclusive", "count") decode c
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

case class GetField(field:String) extends GetSpec {
  override def asJson = ("spec" := "field") ->: ("field" := field) ->: super.asJson
}

case class GetListContains(field:String, value:ValueContainer) extends GetSpec {
  override def asJson = ("spec" := "contains") ->: ("field" := field) ->: ("value" := value) ->: super.asJson
}

case object GetAll extends GetSpec {
  def apply(a:Any) = this
  override def asJson = ("spec" := "all") ->: super.asJson
}

case class GetRelative(forward:Boolean, baseId:Option[String], inclusive:Boolean, count:Int) extends GetSpec {
  override def asJson = ("spec" := "relative") ->: ("forward" := forward) ->: ("baseId" := baseId) ->: ("inclusive" := inclusive) ->: ("count" := count) ->: super.asJson
}

/*
object GetRelative {
  def first(count:Int = 1):OpSpec = GetRelative(true, None, true, count)
  def last(count:Int = 1):OpSpec = GetRelative(false, None, true, count)
  def at(id:String, count:Int = 1):OpSpec = GetRelative(false, Some(id), true, count)
  def before(id:String, count:Int = 1):OpSpec = GetRelative(false, Some(id), false, count)
  def from(id:String, count:Int = 1):OpSpec = GetRelative(true, Some(id), true, count)
  def after(id:String, count:Int = 1):OpSpec = GetRelative(true, Some(id), false, count)
}*/

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

