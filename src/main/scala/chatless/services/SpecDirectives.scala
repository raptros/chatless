package chatless.services
import chatless.fromStringUnmarshaller
import spray.httpx.unmarshalling.Deserializer._
import spray.httpx.unmarshalling._

import chatless.db.{ReplaceField, GetFields, OpSpec}
import spray.routing._
import argonaut._
import Argonaut._
import argonaut.DecodeJson.JBooleanDecodeJson
import argonaut.EncodeJson.JBooleanEncodeJson

trait SpecDirectives { this:ServiceBase =>
  def booleanField(field:String):Directive1[OpSpec] = {
    val pathD = path(field / PathEnd)
    val getF = get & pathD & provide(GetFields(field))
    val putF:Directive1[OpSpec] = (put & pathD & dEntity(as[Boolean])) as { v:Boolean => ReplaceField(field, v) }
    getF | putF
  }
}
