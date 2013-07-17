package chatless.services
import chatless.fromStringUnmarshaller
import chatless.jsonFromString
import spray.httpx.unmarshalling.Deserializer._
import spray.httpx.unmarshalling._

import chatless.db._
import spray.routing._
import argonaut._
import Argonaut._
import shapeless._

trait SpecDirectives { this:ServiceBase =>

  def fieldPathGet(field:String):Directive1[OpSpec] = path(field / PathEnd) & provide(GetFields(field).asInstanceOf[OpSpec])

  def fieldPathReplace[A:Unmarshaller](field:String)(vcCon:A => ValueContainer):Directive1[OpSpec] = path(field / PathEnd) & dEntity(as[A]) hflatMap {
    case v :: HNil => provide(ReplaceField(field, vcCon(v)))
  }

  def listPathItem(field:String)(opCons:(String, ValueContainer) => OpSpec):Directive1[OpSpec] = path(field / Segment / PathEnd) hflatMap {
    case target :: HNil => provide(opCons(field, StringVC(target)))
  }

  def listPathItemTest(field:String):Directive1[OpSpec] = listPathItem(field) { GetListContains.apply _ }

  def listPathItemAppend(field:String):Directive1[OpSpec] = listPathItem(field) { AppendToList.apply _ }

  def listPathItemDelete(field:String):Directive1[OpSpec] = listPathItem(field) { DeleteFromList.apply _ }

  def createJson:Directive1[OpSpec] = dEntity(as[Json]) map { j:Json => Create(JsonVC(j)).asInstanceOf[OpSpec] }
}
