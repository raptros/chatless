package chatless.services.clientApi

import chatless._
import chatless.db._

import scalaz.syntax.id._
import scalaz.syntax.std.boolean._

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
import scalaz.syntax.id._
import chatless.model._
import chatless.services._
import com.google.inject.Inject
import scala.Some
import chatless.responses.{BoolR, StringR, UserNotFoundError}

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.native.Serialization.write
import akka.actor.ActorLogging
import chatless.events.model.{Delta, UserDeltas}

trait MeApi extends ServiceBase {

  val userDao: UserDAO

  private val getUser = (uid: UserId) => userDao get uid getOrElse { throw UserNotFoundError(uid) }

  def setContains[A](v: A): Set[A] => Boolean = _ contains v

  private def completeWithUserSetCheck(uid: UserId)(field: User => Set[String])(value: String): Route =
    complete {
        if (uid |> getUser |> field |> setContains(value)) StatusCodes.NoContent else StatusCodes.NotFound
      }

  private def fieldQuery[A <% JValue](field: String)(value: User => A): CallerRoute = cid => path(field / PathEnd) {
    complete {
      Map(field -> { cid |> getUser |> value })
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
    path(User.FOLLOWING / Segment / PathEnd) {
      completeCheck { _.following }
    } ~ path(User.FOLLOWERS / Segment / PathEnd) {
      completeCheck { _.followers }
    } ~ path(User.BLOCKED / Segment / PathEnd) {
      completeCheck { _.blocked }
    } ~ path(User.TOPICS / Segment / PathEnd) {
      completeCheck { _.topics }
    } ~ path(User.TAGS / Segment / PathEnd) {
      completeCheck { _.tags }
    }
  }

  private def logDelta(delta: Delta) {
    log.info("meApi: delta {}", write(delta))
  }

  private def setNick(cid: UserId, newNick: String) = validate(!newNick.isEmpty, "invalid nick") {
    completeDBOp(userDao.setNick(cid, newNick)) {
      logDelta(UserDeltas.SetNick(cid, newNick))
    }
  }

  private def setPublic(cid: UserId, v: Boolean) = completeDBOp(userDao.setPublic(cid, v)) {
    logDelta(UserDeltas.SetPublic(cid, v))
  }

  private def setInfo(cid: UserId, v: JObject) = completeDBOp(userDao.setInfo(cid, JDoc(v.obj))) {
    logDelta(UserDeltas.SetInfo(cid, JDoc(v.obj)))
  }

  private def followUser(cid: UserId, uid: UserId) =
    optionJsonEntity { oj =>
      complete {
        StringR("whatever")
      }
    }

  private def blockUser(cid: UserId, uid: UserId) = complete {
    "watever"
  }

  private def joinTopic(cid: UserId, tid: TopicId) = optionJsonEntity { m: Option[JDoc] =>
    complete {
      "joinTopic"
    }
  }

  private def addTag(cid: UserId, tag: String) = completeDBOp(userDao.addTag(cid, tag)) {
    log.info("meApi: added tag {} for user {}", tag, cid)
  }

  private def unfollow(cid: UserId, uid: UserId) = complete { "whatever" }

  private def removeFollower(cid: UserId, uid: UserId) = complete { "whatever" }

  private def unblockUser(cid: UserId, uid: UserId) = completeDBOp(userDao.removeBlocked(cid, uid)) {
    log.info("meApi: unblocked user {} for user {}", uid, cid)
  }

  private def leaveTopic(cid: UserId, tid: TopicId) = complete {
    "ugh"
  }

  private def removeTag(cid: UserId, tag: String) = completeDBOp(userDao.removeTag(cid, tag)) {
    log.info("meApi: removed tag {} for user {}", tag, cid)
  }

  private def allPuts(cid: UserId) =
    path(User.NICK / PathEnd) {
      entity(fromString[String]) {
        setNick(cid, _)
      }
    } ~ path(User.PUBLIC / PathEnd) {
      entity(fromString[Boolean]) {
        setPublic(cid, _)
      }
    } ~ path(User.INFO / PathEnd) {
      entity(as[JObject]) {
        setInfo(cid, _)
      }
    } ~ path(User.FOLLOWING / Segment / PathEnd) {
      followUser(cid, _)
    } ~ path(User.BLOCKED / Segment / PathEnd) {
      blockUser(cid, _)
    } ~ path(User.TOPICS / Segment / PathEnd) {
      joinTopic(cid, _)
    } ~ path(User.TAGS / Segment / PathEnd) {
      addTag(cid, _)
    }

  private def allDeletes(cid: UserId) =
    path(User.FOLLOWING / Segment / PathEnd) {
      unfollow(cid, _)
    } ~ path(User.FOLLOWERS / Segment / PathEnd) {
      removeFollower(cid, _)
    } ~ path(User.BLOCKED / Segment / PathEnd) {
      unblockUser(cid, _)
    } ~ path(User.TOPICS / Segment / PathEnd) {
      leaveTopic(cid, _)
    } ~ path(User.TAGS / Segment / PathEnd) {
      removeTag(cid, _)
    }


  val meApi: CallerRoute = cid => resJson {
    pathPrefix(ME_API_BASE) {
      get {
        path(PathEnd) {
          complete {
            getUser(cid)
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
