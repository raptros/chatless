package chatless.services
import chatless.fromStringUnmarshaller
import chatless.jsonFromString
import spray.httpx.unmarshalling.Deserializer._
import spray.httpx.unmarshalling._

import chatless.db._
import spray.routing._
import argonaut._
import Argonaut._

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

  def stringListField(field:String)
}
