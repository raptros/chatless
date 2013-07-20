package chatless.operation

import argonaut._
import Argonaut._

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

