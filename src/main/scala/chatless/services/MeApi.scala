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

import scalaz.std.function._
import scalaz.syntax.semigroup._

trait MeApi extends ServiceBase {
  val ME_API_BASE="me"

  def getFieldsRoute(cid:UserId):Route =
    path(PathEnd) {
      completeJson { dbac.getUser(cid, cid) }
    } ~ path(UserM.UID / PathEnd) {
      completeString { dbac.getUser(cid, cid) map { _.uid } }
    } ~ path(UserM.NICK / PathEnd) {
      completeString { dbac.getUser(cid, cid) map { _.nick } }
    }~ path(UserM.PUBLIC / PathEnd) {
      completeBoolean { dbac.getUser(cid, cid) map { _.public } }
    } ~ path(UserM.INFO / PathEnd) {
      completeJson { dbac.getUser(cid, cid) map { _.info } }
    } ~ path(UserM.FOLLOWING / PathEnd) {
      completeJson { dbac.getUser(cid, cid) map { _.following } }
    } ~ path(UserM.FOLLOWERS / PathEnd) {
      completeJson { dbac.getUser(cid, cid) map { _.followers } }
    } ~ path(UserM.BLOCKED / PathEnd) {
      completeJson { dbac.getUser(cid, cid) map { _.blocked } }
    } ~ path(UserM.TOPICS / PathEnd) {
      completeJson { dbac.getUser(cid, cid) map { _.topics } }
    } ~ path(UserM.TAGS / PathEnd) {
      completeJson { dbac.getUser(cid, cid) map { _.tags } }
    }

  private def getUserListContains(uid:UserId, field: UserM => Set[String])(value:String):Future[Boolean] = {
    dbac.getUser(uid, uid) map { field } map { _ contains value }
  }

  def querySetsRoute(uid:UserId):Route = (
    (path(UserM.FOLLOWING / Segment / PathEnd) map { getUserListContains(uid, _.following) })
    | (path(UserM.FOLLOWERS / Segment / PathEnd) map { getUserListContains(uid, _.followers) })
    | (path(UserM.BLOCKED / Segment / PathEnd) map { getUserListContains(uid, _.blocked) })
    | (path(UserM.TOPICS / Segment / PathEnd) map { getUserListContains(uid, _.topics) })
    | (path(UserM.TAGS / Segment / PathEnd) map { getUserListContains(uid, _.tags) })
    ) { b:Future[Boolean] => completeBoolean(b) }

  def replaceFields(cid:UserId):Route = path(UserM.NICK / PathEnd) {
    dEntity(as[String]) { v =>
      onSuccess(dbac.updateUser(cid, cid, ReplaceNick(v))) { completeBoolean }
    }
  } ~ path(UserM.PUBLIC / PathEnd) {
    dEntity(as[Boolean]) { v =>
      onSuccess(dbac.updateUser(cid, cid, SetPublic(v))) { completeBoolean }
    }
  } ~ path(UserM.INFO / PathEnd) {
    dEntity(as[Json]) { v =>
      onSuccess(dbac.updateUser(cid, cid, UpdateInfo(v))) { completeBoolean }
    }
  }

  def addToSets(cid:UserId):Route =
    path(UserM.FOLLOWING / Segment / PathEnd) { u:UserId =>
      optionJsonEntity { oj =>
        onSuccess(dbac.updateUser(cid, cid, FollowUser(u, oj))) { completeBoolean }
      }
    } ~ path(UserM.BLOCKED / Segment / PathEnd) { u:UserId =>
      onSuccess(dbac.updateUser(cid, cid, BlockUser(u))) { completeBoolean }
    } ~ path(UserM.TOPICS / Segment / PathEnd) { t:TopicId =>
      optionJsonEntity { oj =>
        onSuccess(dbac.updateUser(cid, cid, JoinTopic(t, oj))) { completeBoolean }
      }
    } ~ path(UserM.TAGS / Segment / PathEnd) { t:String =>
      onSuccess(dbac.updateUser(cid, cid, AddTag(t))) { completeBoolean }
    }

  def deleteFromSets(cid:UserId):Route =
    path(UserM.FOLLOWING / Segment / PathEnd) { u:UserId =>
      onSuccess(dbac.updateUser(cid, cid, UnfollowUser(u))) { completeBoolean }
    } ~ path(UserM.FOLLOWERS / Segment / PathEnd) { u:UserId =>
      onSuccess(dbac.updateUser(cid, cid, RemoveFollower(u))) { completeBoolean }
    } ~ path(UserM.BLOCKED / Segment / PathEnd) { u:UserId =>
      onSuccess(dbac.updateUser(cid, cid, UnblockUser(u))) { completeBoolean }
    } ~ path(UserM.TOPICS / Segment / PathEnd) { t:TopicId =>
      onSuccess(dbac.updateUser(cid, cid, LeaveTopic(t))) { completeBoolean }
    } ~ path(UserM.TAGS / Segment / PathEnd) { t:String =>
      onSuccess(dbac.updateUser(cid, cid, RemoveTag(t))) { completeBoolean }
    }


  def meApi(cid:UserId):Route =
    pathPrefix(ME_API_BASE) {
      get {
        getFieldsRoute(cid) ~ querySetsRoute(cid)
      } ~ put {
        replaceFields(cid) ~ addToSets(cid)
      } ~ delete { deleteFromSets(cid) }
    }
}
