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

  def providePathWith[A](lastSeg:String, a:A):Directive1[A] = path(lastSeg / PathEnd) & provide(a)

  def getFieldsRoute(user:UserM):Route = (
    path("uid" / PathEnd) { completeString(user.uid) }
    ~ path("nick" / PathEnd) { completeString(user.nick) }
    ~ path("public" / PathEnd) { completeBoolean(user.public) }
    ~ path("info" / PathEnd) { completeJson(user.info) }
    ~ path("following" / PathEnd) { completeJson(user.following) }
    ~ path("followers" / PathEnd) { completeJson(user.followers) }
    ~ path("blocked" / PathEnd) { completeJson(user.blocked) }
    ~ path("topics" / PathEnd) { completeJson(user.topics) }
    ~ path("tags" / PathEnd) { completeJson(user.tags) }
    )

  def querySetsRoute(user:UserM):Route = (
    (path("following" / Segment / PathEnd) map { s:String => user.following contains s })
    | (path("followers" / Segment / PathEnd) map { s:String => user.followers contains s })
    | (path("blocked" / Segment / PathEnd) map { s:String => user.blocked contains s })
    | (path("topics" / Segment / PathEnd) map { s:String => user.topics contains s })
    | (path("tags" / Segment / PathEnd) map { s:String => user.tags contains s })
    ) { b:Boolean => completeBoolean(b) }

  def meApi2 = userAuth { cid =>
    pathPrefix("me") {
      get {
        onSuccess(dbac.getUser(cid, cid)) { user:UserM =>
          path(PathEnd) {
            completeAsJson(user)
          } ~ getFieldsRoute(user) ~ querySetsRoute(user)
        }
      }
    }
  }
}
