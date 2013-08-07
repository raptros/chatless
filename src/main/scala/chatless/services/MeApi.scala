package chatless.services

import argonaut._
import Argonaut._

import chatless._
import chatless.op2._
import chatless.db._

import spray.httpx.unmarshalling._
import shapeless._

import spray.routing._
import HListDeserializer._

import spray.httpx.unmarshalling.Deserializer._
import spray.httpx.encoding.NoEncoding

import spray.http._
import MediaTypes._
import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.function._
import scalaz.syntax.semigroup._
import chatless.models.UserM
import akka.actor.ActorRefFactory

class MeApi(val dbac: DatabaseAccessor)(implicit val actorRefFactory: ActorRefFactory)
  extends CallerRoute with ServiceBase {

  val ME_API_BASE = "me"

  type CUU = UpdateSpec with ForUsers => Route

  private def completeWithUserSetCheck(uid: UserId)(field: UserM => Set[String])(value: String): Route = completeBoolean {
    dbac.getUser(uid, uid) map { field } map { _ contains value }
  }

  def completeUpdateUser(cid: UserId)(op: UpdateSpec with ForUsers): Route =
    onSuccess(dbac.updateUser(cid, cid, op)) { completeBoolean }

  def getFieldsRoute(cid: UserId): Route =
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

  def querySetsRoute(uid: UserId): Route = {
    val completeCheck = completeWithUserSetCheck(uid) _
    path(UserM.FOLLOWING / Segment / PathEnd) {
      completeCheck { _.following }
    } ~ path(UserM.FOLLOWERS / Segment / PathEnd) {
      completeCheck { _.followers }
    } ~ path(UserM.BLOCKED / Segment / PathEnd) {
      completeCheck { _.blocked }
    } ~ path(UserM.TOPICS / Segment / PathEnd) {
      completeCheck { _.topics }
    } ~ path(UserM.TAGS / Segment / PathEnd) {
      completeCheck { _.tags }
    }
  }

  def replaceFields(cuu: CUU): Route = path(UserM.NICK / PathEnd) {
    dEntity(as[String]) { v => cuu { ReplaceNick(v) } }
  } ~ path(UserM.PUBLIC / PathEnd) {
    dEntity(as[Boolean]) { v => cuu { SetPublic(v) } }
  } ~ path(UserM.INFO / PathEnd) {
    dEntity(as[Json]) { v => cuu { UpdateInfo(v) } }
  }

  def addToSets(cuu: CUU): Route =
    path(UserM.FOLLOWING / Segment / PathEnd) { u: UserId =>
      optionJsonEntity { oj => cuu { FollowUser(u, oj) } }
    } ~ path(UserM.BLOCKED / Segment / PathEnd) { u: UserId =>
      cuu { BlockUser(u) }
    } ~ path(UserM.TOPICS / Segment / PathEnd) { t: TopicId =>
      optionJsonEntity { oj => cuu { JoinTopic(t, oj) } }
    } ~ path(UserM.TAGS / Segment / PathEnd) { t: String =>
      cuu { AddTag(t) }
    }

  def deleteFromSets(cuu: CUU): Route =
    path(UserM.FOLLOWING / Segment / PathEnd) { u: UserId =>
      cuu { UnfollowUser(u) }
    } ~ path(UserM.FOLLOWERS / Segment / PathEnd) { u: UserId =>
      cuu { RemoveFollower(u) }
    } ~ path(UserM.BLOCKED / Segment / PathEnd) { u: UserId =>
      cuu { UnblockUser(u) }
    } ~ path(UserM.TOPICS / Segment / PathEnd) { t: TopicId =>
      cuu { LeaveTopic(t) }
    } ~ path(UserM.TAGS / Segment / PathEnd) { t: String =>
      cuu { RemoveTag(t) }
    }

  def updateUser(cid: UserId): Route = {
    val cuu = completeUpdateUser(cid) _
    put {
      replaceFields(cuu) ~ addToSets(cuu)
    } ~ delete {
      deleteFromSets(cuu)
    }
  }

  def apply(cid: UserId): Route = pathPrefix(ME_API_BASE) {
    get { getFieldsRoute(cid) ~ querySetsRoute(cid) } ~ updateUser(cid)
  }
}
