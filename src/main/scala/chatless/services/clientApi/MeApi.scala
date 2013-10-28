package chatless.services.clientApi

import chatless._
import chatless.db._


import spray.routing._

import spray.httpx.unmarshalling.Deserializer._

import spray.http._

import scalaz.syntax.semigroup._
import scalaz.std.function._
import scalaz.syntax.id._
import chatless.model._
import chatless.services._

import org.json4s._
import org.json4s.JsonDSL._
import chatless.ops.UserOps

trait MeApi extends ServiceBase {

  val userOps: UserOps

  def setContains[A](v: A): Set[A] => Boolean = _ contains v

  private def mkMap[A](s: String)(v: A) = new Map.Map1[String, A](s, v)

  private def completeWithUserSetCheck(uid: UserId)(field: User => Set[String])(value: String): Route =
    complete {
        if (uid |> userOps.getOrThrow |> field |> setContains(value)) StatusCodes.NoContent else StatusCodes.NotFound
      }

  private def fieldQuery[A <% JValue](field: String)(value: User => A): CallerRoute = cid => path(field) {
    resJson {
      complete {
        cid |> userOps.getOrThrow |> value |> mkMap[A](field)
      }
    }
  }

  private val getFields: CallerRoute =
    fieldQuery(User.ID) { _.id } |+|
    fieldQuery(User.NICK) { _.nick } |+|
    fieldQuery(User.PUBLIC) { _.public } |+|
    fieldQuery(User.INFO) { _.info } |+|
    fieldQuery(User.FOLLOWING) { _.following } |+|
    fieldQuery(User.FOLLOWERS) { _.followers } |+|
    fieldQuery(User.BLOCKED) { _.blocked } |+|
    fieldQuery(User.TOPICS) { _.topics } |+|
    fieldQuery(User.TAGS) { _.tags }

  private val querySetsRoute: CallerRoute = cid => get {
    val completeCheck = completeWithUserSetCheck(cid) _
    path(User.FOLLOWING / Segment) {
      completeCheck { _.following }
    } ~ path(User.FOLLOWERS / Segment) {
      completeCheck { _.followers }
    } ~ path(User.BLOCKED / Segment) {
      completeCheck { _.blocked }
    } ~ path(User.TOPICS / Segment) {
      completeCheck { _.topics }
    } ~ path(User.TAGS / Segment) {
      completeCheck { _.tags }
    }
  }

  private def setNick(cid: UserId, newNick: String) = validate(!newNick.isEmpty, "invalid nick") {
    completeOp { userOps.setNick(cid, newNick) }
  }

  private def setPublic(cid: UserId, v: Boolean) = completeOp { userOps.setPublic(cid, v) }

  private def setInfo(cid: UserId, v: JObject) = completeOp { userOps.setInfo(cid, JDoc(v.obj)) }

  private def followUser(cid: UserId, uid: UserId) = optionJsonEntity { oj =>
    //todo build the request system to handle follow requests
    completeOp { userOps.followUser(cid, uid) }
  }

  private def blockUser(cid: UserId, uid: UserId) = completeOp { userOps.blockUser(cid, uid) }

  private def joinTopic(cid: UserId, tid: TopicId) = optionJsonEntity { m: Option[JDoc] =>
    //todo see above
    completeOp { userOps.joinTopic(cid, tid) }
  }

  private def unfollow(cid: UserId, uid: UserId) = completeOp { userOps.unfollowUser(cid, uid) }

  private def removeFollower(cid: UserId, uid: UserId) = completeOp { userOps.removeFollower(cid, uid) }

  private def unblockUser(cid: UserId, uid: UserId) = completeOp { userOps.unblockUser(cid, uid) }

  private def leaveTopic(cid: UserId, tid: TopicId) = completeOp { userOps.leaveTopic(cid, tid) }

  private def addTag(cid: UserId, tag: String) = completeOp { userOps.addTag(cid, tag) }

  private def removeTag(cid: UserId, tag: String) = completeOp { userOps.removeTag(cid, tag) }

  private def allPuts(cid: UserId) =
    path(User.NICK) {
      entity(fromString[String]) {
        setNick(cid, _)
      }
    } ~ path(User.PUBLIC) {
      entity(fromString[Boolean]) {
        setPublic(cid, _)
      }
    } ~ path(User.INFO) {
      entity(as[JObject]) {
        setInfo(cid, _)
      }
    } ~ path(User.FOLLOWING / Segment) {
      followUser(cid, _)
    } ~ path(User.BLOCKED / Segment) {
      blockUser(cid, _)
    } ~ path(User.TOPICS / Segment) {
      joinTopic(cid, _)
    } ~ path(User.TAGS / Segment) {
      addTag(cid, _)
    }

  private def allDeletes(cid: UserId) =
    path(User.FOLLOWING / Segment) {
      unfollow(cid, _)
    } ~ path(User.FOLLOWERS / Segment) {
      removeFollower(cid, _)
    } ~ path(User.BLOCKED / Segment) {
      unblockUser(cid, _)
    } ~ path(User.TOPICS / Segment) {
      leaveTopic(cid, _)
    } ~ path(User.TAGS / Segment) {
      removeTag(cid, _)
    }


  val meApi: CallerRoute = cid => {
    pathPrefix(ME_API_BASE) {
      get {
        pathEnd {
          resJson {
            complete {
              userOps.getOrThrow(cid)
            }
          }
        } ~ getFields(cid) ~ querySetsRoute(cid)
      } ~ put {
        allPuts(cid)
      } ~ delete {
        allDeletes(cid)
      }
    }
  }
}
