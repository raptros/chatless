package chatless.operation

import argonaut._
import Argonaut._
import scala.reflect.runtime.universe._

sealed trait ValueContainer {
  type A
  implicit val cj:CodecJson[A]
  def tpe:Type
  def contained:A
}

sealed abstract class AbstractVC[B](implicit tt:TypeTag[B], enc:EncodeJson[B], dec:DecodeJson[B]) extends ValueContainer {
  type A = B
  def tpe = typeOf[B]
  implicit val cj = CodecJson.derived[B](enc, dec)
}

object ValueContainer {

  implicit def VCEncodeJ:EncodeJson[ValueContainer] = EncodeJson { vc:ValueContainer =>
    implicit val cj = vc.cj
    ("type" := vc.tpe.typeSymbol.name.decoded) ->:
      ("contained" := vc.contained) ->:
      jEmptyObject
  }

  implicit def JDecodeVC:DecodeJson[ValueContainer] = DecodeJson { c =>
    (c --\ "type").as[String] flatMap {
      case "Boolean" => (c --\ "contained").as[Boolean] map { BooleanVC }
      case "String" => (c --\ "contained").as[String] map { StringVC }
      case "Json" => (c --\ "contained").as[Json] map { JsonVC }
    }
  }
}

case class BooleanVC(contained:Boolean) extends AbstractVC[Boolean]

case class StringVC(contained:String) extends AbstractVC[String]

case class JsonVC(contained:Json) extends AbstractVC[Json]
