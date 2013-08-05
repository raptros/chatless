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
import scala.concurrent.Future

trait MeApi extends ServiceBase {

  def getFieldsRoute(uid:UserId):Route =
    path("uid" / PathEnd) {
      completeString { dbac.getUser(uid, uid) map { _.uid } }
    } ~ path("nick" / PathEnd) {
      completeString { dbac.getUser(uid, uid) map { _.nick } }
    }~ path("public" / PathEnd) {
      completeBoolean { dbac.getUser(uid, uid) map { _.public } }
    } ~ path("info" / PathEnd) {
      completeJson { dbac.getUser(uid, uid) map { _.info } }
    } ~ path("following" / PathEnd) {
      completeJson { dbac.getUser(uid, uid) map { _.following } }
    } ~ path("followers" / PathEnd) {
      completeJson { dbac.getUser(uid, uid) map { _.followers } }
    } ~ path("blocked" / PathEnd) {
      completeJson { dbac.getUser(uid, uid) map { _.blocked } }
    } ~ path("topics" / PathEnd) {
      completeJson { dbac.getUser(uid, uid) map { _.topics } }
    } ~ path("tags" / PathEnd) {
      completeJson { dbac.getUser(uid, uid) map { _.tags } }
    }

  private def getUserListContains(uid:UserId, field: UserM => Set[String])(value:String):Future[Boolean] = {
    dbac.getUser(uid, uid) map { field } map { _ contains value }
  }

  def querySetsRoute(uid:UserId):Route = (
    (path("following" / Segment / PathEnd) map { getUserListContains(uid, _.following) })
    | (path("followers" / Segment / PathEnd) map { getUserListContains(uid, _.followers) })
    | (path("blocked" / Segment / PathEnd) map { getUserListContains(uid, _.blocked) })
    | (path("topics" / Segment / PathEnd) map { getUserListContains(uid, _.topics) })
    | (path("tags" / Segment / PathEnd) map { getUserListContains(uid, _.tags) })
    ) { b:Future[Boolean] => completeBoolean(b) }

  def optionJsonEntity:Directive1[Option[Json]] = extract { c =>
    c.request.entity.toOption map { _.asString } flatMap { _.parseOption }
  }

  def replaceFields(cid:UserId):Route = path("nick" / PathEnd) {
    dEntity(as[String]) { v =>
      onSuccess(dbac.updateUser(cid, cid, ReplaceNick(v))) { completeBoolean }
    }
  } ~ path("public" / PathEnd) {
    dEntity(as[Boolean]) { v =>
      onSuccess(dbac.updateUser(cid, cid, SetPublic(v))) { completeBoolean }
    }
  } ~ path("info" / PathEnd) {
    dEntity(as[Json]) { v =>
      onSuccess(dbac.updateUser(cid, cid, UpdateInfo(v))) { completeBoolean }
    }
  }

  def addToSets(cid:UserId):Route =
    path("following" / Segment / PathEnd) { u:UserId =>
      optionJsonEntity { oj =>
        onSuccess(dbac.updateUser(cid, cid, FollowUser(u, oj))) { completeBoolean }
      }
    } ~ path("blocked" / Segment / PathEnd) { u:UserId =>
      onSuccess(dbac.updateUser(cid, cid, BlockUser(u))) { completeBoolean }
    } ~ path("topics" / Segment / PathEnd) { t:TopicId =>
      optionJsonEntity { oj =>
        onSuccess(dbac.updateUser(cid, cid, JoinTopic(t, oj))) { completeBoolean }
      }
    } ~ path("tags" / Segment / PathEnd) { t:String =>
      onSuccess(dbac.updateUser(cid, cid, AddTag(t))) { completeBoolean }
    }

  def deleteFromSets(cid:UserId):Route =
    path("following" / Segment / PathEnd) { u:UserId =>
      onSuccess(dbac.updateUser(cid, cid, UnfollowUser(u))) { completeBoolean }
    } ~ path("followers" / Segment / PathEnd) { u:UserId =>
      onSuccess(dbac.updateUser(cid, cid, RemoveFollower(u))) { completeBoolean }
    } ~ path("blocked" / Segment / PathEnd) { u:UserId =>
      onSuccess(dbac.updateUser(cid, cid, UnblockUser(u))) { completeBoolean }
    } ~ path("topics" / Segment / PathEnd) { t:TopicId =>
      onSuccess(dbac.updateUser(cid, cid, LeaveTopic(t))) { completeBoolean }
    } ~ path("tags" / Segment / PathEnd) { t:String =>
      onSuccess(dbac.updateUser(cid, cid, RemoveTag(t))) { completeBoolean }
    }


  def meApi(cid:UserId):Route = pathPrefix("me") {
    get {
      path(PathEnd) {
        completeJson { dbac.getUser(cid, cid) }
      } ~ getFieldsRoute(cid) ~ querySetsRoute(cid)
    } ~ put {
      replaceFields(cid) ~ addToSets(cid)
    } ~ delete {
      deleteFromSets(cid)
    }
  }
}
