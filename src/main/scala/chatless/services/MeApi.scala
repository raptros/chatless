package chatless.services

import argonaut._
import Argonaut._

import chatless._
import chatless.op2._

import spray.httpx.unmarshalling._
import shapeless._

import spray.routing._
import HListDeserializer._

import spray.httpx.unmarshalling.Deserializer._
import spray.httpx.encoding.NoEncoding

import spray.http._
import MediaTypes._

trait MeApi extends ServiceBase {


  implicit val StringCodecJson = CodecJson.derived[String]


  def providePathWith[A:EncodeJson](lastSeg:String, a:A):Directive1[Json] = path(lastSeg / PathEnd) & provide(a.asJson)

  def completeJson(j:Json) = respondWithMediaType(`application/json`) { complete(j.nospaces) }

  def getFieldsRoute(user:UserM):Route = {
    ( providePathWith("uid", List(user.uid))
    | providePathWith("nick", List(user.nick))
    | providePathWith("public", List(user.public))
    | providePathWith("following", user.following)
    | providePathWith("following", user.following)
    ) { completeJson }
  }

  val meApi2 = userAuth { cid =>
    pathPrefix("me") {
      get {
        onSuccess(dbac.getUser(cid, cid)) { user:UserM =>
          path(PathEnd) {
            completeAsJson(user)
          } ~ getFieldsRoute(user)
        }
      }
    }
  }
  /*
          get {
          } ~ putReplacement(as[String]) { e:String =>
            callDBDirective[Boolean](UpdateUser(cid, ReplaceField[String]("nick", e))) { a:Boolean =>
              completeAsJson(a)
            }
          }
        } ~ path("public" / PathEnd) {
          get {
            completeAsJson(List(user.public))
          } ~ putReplacement(as[Boolean]) { e:Boolean =>
            callDBDirective[Boolean](UpdateUser(cid, ReplaceField[Boolean]("public", e))) { a:Boolean =>
              completeAsJson(a)
            }
          }
        }
      }
    }
  }*/
}
