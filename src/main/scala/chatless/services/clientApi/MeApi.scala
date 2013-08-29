package chatless.services.clientApi

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
import chatless.models.{User, UserDAO}
import chatless.services._
import com.google.inject.Inject

trait MeApi extends ServiceBase {

  val userDao: UserDAO

  def getUser(cid: UserId): Directive1[User] = provide((userDao get cid) getOrElse { throw UserNotFoundError(cid) })

  private def completeWithUserSetCheck(uid: UserId)(field: User => Set[String])(value: String): Route =
    getUser(uid) { user: User =>
      complete { field(user) contains value  }
    }

  private val getFieldsRoute: CallerRoute = cid => get {
    getUser(cid) { user: User =>
      path(PathEnd) {
        resJson { complete { user } }
      } ~ path(User.UID / PathEnd) {
        resText { complete { user.uid } }
      } ~ path(User.NICK / PathEnd) {
        resText { complete { user.nick } }
      }~ path(User.PUBLIC / PathEnd) {
        resText { complete { user.public } }
      } ~ path(User.INFO / PathEnd) {
        resJson { complete { user.info } }
      } ~ path(User.FOLLOWING / PathEnd) {
        resJson { complete { user.following } }
      } ~ path(User.FOLLOWERS / PathEnd) {
        resJson { complete { user.followers } }
      } ~ path(User.BLOCKED / PathEnd) {
        resJson { complete { user.blocked } }
      } ~ path(User.TOPICS / PathEnd) {
        resJson { complete { user.topics } }
      } ~ path(User.TAGS / PathEnd) {
        resJson { complete { user.tags } }
      }
    }
  }

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

  private val replaceFields: CallerRoute = cid => put {
    path(User.NICK / PathEnd) {
      dEntity(as[String]) { v =>
        complete("no")
      }
    } ~ path(User.PUBLIC / PathEnd) {
      dEntity(as[Boolean]) { v =>
        complete("no")
      }
    } ~ path(User.INFO / PathEnd) {
      optionJsonEntity {
        case Some(m) => complete { "no" }
        case None => complete { 400 -> "need a json object here" }
      }
    }
  }

  private val addToSets: CallerRoute = cid => put {
    path(User.FOLLOWING / Segment / PathEnd) { u: UserId =>
      optionJsonEntity { oj =>
        complete { "whatever" }
      }
    } ~ path(User.BLOCKED / Segment / PathEnd) { u: UserId =>
      complete { "whatever" }
    } ~ path(User.TOPICS / Segment / PathEnd) { t: TopicId =>
      optionJsonEntity { m: Option[Map[String, Any]] =>
        complete { "whatever" }
      }
    } ~ path(User.TAGS / Segment / PathEnd) { t: String =>
      complete { "whatever" }
    }
  }

  private val deleteFromSets: CallerRoute = cid => delete {
    path(User.FOLLOWING / Segment / PathEnd) { u: UserId =>
      complete { "whatever" }
    } ~ path(User.FOLLOWERS / Segment / PathEnd) { u: UserId =>
      complete { "whatever" }
    } ~ path(User.BLOCKED / Segment / PathEnd) { u: UserId =>
      complete { "whatever" }
    } ~ path(User.TOPICS / Segment / PathEnd) { t: TopicId =>
      complete { "whatever" }
    } ~ path(User.TAGS / Segment / PathEnd) { t: String =>
      complete { "whatever" }
    }
   }


  private val cr: CallerRoute = getFieldsRoute |+| querySetsRoute |+| addToSets |+| deleteFromSets

  val meApi: CallerRoute = cid => pathPrefix(ME_API_BASE) {
    getFieldsRoute(cid) ~ querySetsRoute(cid) ~ replaceFields(cid) ~ addToSets(cid) ~ deleteFromSets(cid)
  }
}
