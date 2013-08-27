package chatless.services.clientApi

import argonaut._
import Argonaut._

import chatless._
import chatless.op2._
import chatless.db._

import scalaz.syntax.id._

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
import chatless.models.{User, UserDAO, UserM}
import akka.actor.ActorRefFactory
import chatless.services._
import chatless.op2.ReplaceNick
import chatless.op2.RemoveFollower
import chatless.op2.UnfollowUser
import chatless.op2.UnblockUser
import chatless.op2.JoinTopic
import chatless.op2.LeaveTopic
import chatless.op2.UpdateInfo
import chatless.op2.BlockUser
import chatless.op2.RemoveTag
import chatless.op2.FollowUser
import chatless.op2.SetPublic
import chatless.op2.AddTag
import chatless.op2.ReplaceNick
import chatless.op2.RemoveFollower
import chatless.op2.UnfollowUser
import chatless.op2.UnblockUser
import chatless.op2.JoinTopic
import chatless.op2.LeaveTopic
import chatless.op2.UpdateInfo
import chatless.op2.BlockUser
import chatless.op2.RemoveTag
import chatless.op2.FollowUser
import chatless.op2.SetPublic
import chatless.op2.AddTag
import spray.routing.PathMatchers.PathEnd
import com.google.inject.Inject

trait MeApi extends ServiceBase {

  val userDao: UserDAO

  private type CUU = UpdateSpec with ForUsers => Route

  def getUser(cid: UserId): Directive1[User] = provide((userDao get cid) getOrElse { throw UserNotFoundError(cid) })

  private def completeWithUserSetCheck(uid: UserId)(field: User => Set[String])(value: String): Route = getCaller(uid) { user: User =>
    complete {
      field(user) contains value
    }
  }

  private def completeUpdateUser(cid: UserId)(op: UpdateSpec with ForUsers): Route = complete {
  }

  private def getFieldsRoute(cid: UserId): Route = getUser(cid) { user: User =>
    path(PathEnd) {
      complete { user }
    } ~ path(User.UID / PathEnd) {
      completeString { user.uid }
    } ~ path(User.NICK / PathEnd) {
      completeString { user.nick }
    }~ path(User.PUBLIC / PathEnd) {
      completeBoolean { user.public }
    } ~ path(User.INFO / PathEnd) {
      completeJson { user.info }
    } ~ path(User.FOLLOWING / PathEnd) {
      completeJson { user.following }
    } ~ path(User.FOLLOWERS / PathEnd) {
      completeJson { user.followers }
    } ~ path(User.BLOCKED / PathEnd) {
      completeJson { user.blocked }
    } ~ path(User.TOPICS / PathEnd) {
      completeJson { user.topics }
    } ~ path(User.TAGS / PathEnd) {
      completeJson { user.tags }
    }
  }

  private def querySetsRoute(uid: UserId): Route = {
    val completeCheck = completeWithUserSetCheck(uid) _
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

  private def replaceFields(cuu: CUU): Route = path(User.NICK / PathEnd) {
    dEntity(as[String]) { v => cuu { ReplaceNick(v) } }
  } ~ path(User.PUBLIC / PathEnd) {
    dEntity(as[Boolean]) { v => cuu { SetPublic(v) } }
  } ~ path(User.INFO / PathEnd) {
    dEntity(as[Json]) { v => cuu { UpdateInfo(v) } }
  }

  private def addToSets(cuu: CUU): Route =
    path(User.FOLLOWING / Segment / PathEnd) { u: UserId =>
      optionJsonEntity { oj => cuu { FollowUser(u, oj) } }
    } ~ path(User.BLOCKED / Segment / PathEnd) { u: UserId =>
      cuu { BlockUser(u) }
    } ~ path(User.TOPICS / Segment / PathEnd) { t: TopicId =>
      optionJsonEntity { oj => cuu { JoinTopic(t, oj) } }
    } ~ path(User.TAGS / Segment / PathEnd) { t: String =>
      cuu { AddTag(t) }
    }

  private def deleteFromSets(cuu: CUU): Route =
    path(User.FOLLOWING / Segment / PathEnd) { u: UserId =>
      cuu { UnfollowUser(u) }
    } ~ path(User.FOLLOWERS / Segment / PathEnd) { u: UserId =>
      cuu { RemoveFollower(u) }
    } ~ path(User.BLOCKED / Segment / PathEnd) { u: UserId =>
      cuu { UnblockUser(u) }
    } ~ path(User.TOPICS / Segment / PathEnd) { t: TopicId =>
      cuu { LeaveTopic(t) }
    } ~ path(User.TAGS / Segment / PathEnd) { t: String =>
      cuu { RemoveTag(t) }
    }

  private def updateUser(cid: UserId): Route = {
    val cuu = completeUpdateUser(cid) _
    put {
      replaceFields(cuu) ~ addToSets(cuu)
    } ~ delete {
      deleteFromSets(cuu)
    }
  }

  val meApi: CallerRoute = cid => pathPrefix(ME_API_BASE) {
    get { getFieldsRoute(cid) ~ querySetsRoute(cid) } ~ updateUser(cid)
  }
}
