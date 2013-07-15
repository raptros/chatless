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
  def booleanField(field:String):Directive1[OpSpec] = {
    val pathD = path(field / PathEnd)
    val getF = get & pathD & provide(GetFields(field))
    val putF:Directive1[OpSpec] = (put & pathD & dEntity(as[Boolean])) as { v:Boolean => ReplaceField(field, BooleanVC(v)) }
    getF | putF
  }

  def stringField(field:String):Directive1[OpSpec] = {
    val pathD = path(field / PathEnd)
    val getF = get & pathD & provide(GetFields(field))
    val putF:Directive1[OpSpec] = (put & pathD & dEntity(as[String])) as { v:String => ReplaceField(field, StringVC(v)) }
    getF | putF
  }

  def jsonField(field:String):Directive1[OpSpec] = {
    val pathD = path(field / PathEnd)
    val getF = get & pathD & provide(GetFields(field))
    val putF:Directive1[OpSpec] = (put & pathD & dEntity(as[Json])) as { v:Json => ReplaceField(field, JsonVC(v)) }
    getF | putF
  }

  def replacePathField[A:Unmarshaller](field:String)(vcCon:A => ValueContainer):Directive1[OpSpec] = path(field / PathEnd) & dEntity(as[A]) hflatMap {
    case v :: HNil => provide(ReplaceField(field, vcCon(v)))
  }

  def replaceBoolean(field:String):Directive1[OpSpec] = dEntity(as[Boolean]) as {
    v:Boolean => ReplaceField(field, BooleanVC(v))
  }

  def replaceString(field:String):Directive1[OpSpec] = dEntity(as[String]) as {
    v:String => ReplaceField(field, StringVC(v))
  }


  def replaceJson(field:String):Directive1[OpSpec] = dEntity(as[Json]) as {
    v:Json => ReplaceField(field, JsonVC(v))
  }

  def listPathItem(field:String)(opCons:(String, ValueContainer) => OpSpec):Directive1[OpSpec] = path(field / Segment / PathEnd) hflatMap {
    case target :: HNil => provide(opCons(field, StringVC(target)))
  }

  def getPathField(field:String):Directive1[OpSpec] = path(field / PathEnd) & provide(GetFields(field).asInstanceOf[OpSpec])


  def testListPath(field:String):Directive1[OpSpec] = listPathItem(field) { GetListContains.apply _ }

  def addListPath(field:String):Directive1[OpSpec] = listPathItem(field) { AppendToList.apply _ }

  def deleteListPath(field:String):Directive1[OpSpec] = listPathItem(field) { DeleteFromList.apply _ }

  def getListContainsString(field:String, target:String):Directive1[OpSpec] = get &
    provide(GetListContains(field, StringVC(target)).asInstanceOf[OpSpec])

  def appendStringToList(field:String, target:String):Directive1[OpSpec] = put &
    provide(AppendToList(field, StringVC(target)).asInstanceOf[OpSpec])

  def deleteStringFromList(field:String, target:String):Directive1[OpSpec] = delete &
    provide(DeleteFromList(field, StringVC(target)).asInstanceOf[OpSpec])

  def getFields(field:String*):Directive1[OpSpec] = get & {
    provide(GetFields(field : _*).asInstanceOf[OpSpec])
  }

  /*
  type FieldSpec = String => OpSpec

  def fieldOperation(on:Directive[UserId :: OpRes :: HNil], field:String,
                      getSpec:Option[FieldSpec] = Some { f => GetFields(f) },
                      replaceSpec:Option[FieldSpec] = None) = {
  }*/

//  def stringListField(field:String)
}
