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

  def listPathItemTest(field:String):Directive1[OpSpec] = listPathItem(field) { GetListContains apply _ }

  def listPathItemAppend(field:String):Directive1[OpSpec] = listPathItem(field) { AppendToList apply _ }

  def listPathItemDelete(field:String):Directive1[OpSpec] = listPathItem(field) { DeleteFromList apply _ }

  def createJson:Directive1[OpSpec] = dEntity(as[Json]) map { j:Json => Create(JsonVC(j)).asInstanceOf[OpSpec] }


  class Relative(prefix:String, forward:Boolean, getId:Boolean, inclusive:Boolean) {
    import Relative._

    private lazy val pre:FrontSpec = if (getId) {
      pathPrefix(prefix / Segment ) map { id:String => forward :: id.some :: inclusive :: HNil }
    } else {
      pathPrefix(prefix) & hprovide(forward :: none[String] :: inclusive :: HNil)
    }

    private lazy val inner:RelativeSpec = pre & count

    lazy val mk:Directive1[OpSpec] = inner as { getRelative }

    def apply(prior:PriorDirective):DOperation = (get & prior & mk) as { operation }
  }

  object Relative {
    type RelativeSpec = Directive[Boolean :: Option[String] :: Boolean :: Int :: HNil]
    type FrontSpec = Directive[Boolean :: Option[String] :: Boolean :: HNil]
    type PriorDirective = Directive[UserId :: OpRes :: HNil]

    type MakeRel = PriorDirective => DOperation

    private val count:Directive1[Int] = path(IntNumber / PathEnd) | (path(PathEnd ) & provide(1))
    private val getRelative: (Boolean, Option[String], Boolean, Int) => OpSpec = { GetRelative apply _ }

    def apply(prefix:String, forward:Boolean, getId:Boolean, inclusive:Boolean):MakeRel = new Relative(prefix, forward, getId, inclusive) apply _

    def at:MakeRel = apply("at", false, true, true)

    def before:MakeRel = apply("before", false, true, false)

    def from:MakeRel = apply("from", true, true, true)

    def after:MakeRel = apply("after", true, true, false)

    def first:MakeRel = apply("first", true, false, true)

    def last:MakeRel = apply("last", false, false, true)

  }

  /*
  val relative:Directive1[OpSpec] = {
    val at:FrontSpec = pathPrefix("at") & hprovide(false :: None :: true :: HNil)
    val

  }*/
}