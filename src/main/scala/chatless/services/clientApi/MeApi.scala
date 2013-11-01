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

  val mkJObj: Pair[String, JValue] => JValue = p => JObject(p)

  private def mapExtractors(extractors: Seq[(String, User => JValue)]) = extractors.toMap map {
    case (field, extractFunc) => field -> { extractFunc andThen field.-> andThen mkJObj }
  }

  private def queryFields(extractors: (String, User => JValue)*): CallerRoute = cid => {
    path(mapExtractors(extractors)) { extractor =>
      resJson {
        complete {
          extractor(userOps.getOrThrow(cid))
        }
      }
    }
  }

  def setChecks(setFields: (String, User => Set[String])*): CallerRoute = cid => {
    path(setFields.toMap / Segment) { (extractor, value) =>
      complete {
        if (cid |> userOps.getOrThrow |> extractor |> setContains(value)) StatusCodes.NoContent else StatusCodes.NotFound
      }
    }
  }

  private val renderUser: CallerRoute = cid => pathEnd { resJson { complete { userOps.getOrThrow(cid) } } }

  private val getFields: CallerRoute = queryFields(
    User.ID        -> { _.id },
    User.NICK      -> { _.nick },
    User.PUBLIC    -> { _.public },
    User.INFO      -> { _.info },
    User.FOLLOWING -> { _.following },
    User.FOLLOWERS -> { _.followers },
    User.BLOCKED   -> { _.blocked },
    User.TOPICS    -> { _.topics },
    User.TAGS      -> { _.tags }
  )

  private val querySetsRoute: CallerRoute = setChecks(
    User.FOLLOWING -> { _.following },
    User.FOLLOWERS -> { _.followers },
    User.BLOCKED   -> { _.blocked },
    User.TOPICS    -> { _.topics },
    User.TAGS      -> { _.tags }
  )

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
        cid |> { renderUser |+| getFields  |+| querySetsRoute }
      } ~ put {
        allPuts(cid)
      } ~ delete {
        allDeletes(cid)
      }
    }
  }
}
