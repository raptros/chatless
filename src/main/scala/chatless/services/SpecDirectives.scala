package chatless.services
import chatless.fromStringUnmarshaller
import chatless.jsonFromString
import spray.httpx.unmarshalling.Deserializer._
import spray.httpx.unmarshalling._

import scalaz.std.option._
import scalaz.syntax.std.option._

import chatless._
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

  object Relative {
    type RelativeSpec = Directive[Boolean :: Option[String] :: Boolean :: Int :: HNil]
    type FrontSpec = Directive[Boolean :: Option[String] :: Boolean :: HNil]

    private val count:Directive1[Int] = path(IntNumber / PathEnd) | (path(PathEnd) & provide(1))
    private val getRelative: (Boolean, Option[String], Boolean, Int) => OpSpec = { GetRelative.apply _ }


    def at:Directive1[OpSpec] = relFor("at", false, true, true)

    def before:Directive1[OpSpec] = relFor("before", false, true, false)

    def from:Directive1[OpSpec] = relFor("from", true, true, true)

    def after:Directive1[OpSpec] = relFor("after", true, true, false)

    def first:Directive1[OpSpec] = relFor("first", true, false, true)

    def last:Directive1[OpSpec] = relFor("last", false, false, true)

    def default(forward:Boolean):Directive1[OpSpec] = path(PathEnd) & provide(GetRelative(forward, none, true, 1).asInstanceOf[OpSpec])

    def relFor(prefix:String, forward:Boolean, getId:Boolean, inclusive:Boolean):Directive1[OpSpec] = {
      val pre:FrontSpec = if (getId) {
        pathPrefix(prefix / Segment) map { id:String => forward :: id.some :: inclusive :: HNil }
      } else {
        pathPrefix(prefix) & hprovide(forward :: none[String] :: inclusive :: HNil)
      }

      (pre & count) as { getRelative }
    }
  }
}