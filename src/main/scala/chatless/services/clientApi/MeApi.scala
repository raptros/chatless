package chatless.services.clientApi

import chatless._
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
import chatless.model._
import chatless.services._
import com.google.inject.Inject
import scala.Some
import chatless.responses.{BoolR, StringR, UserNotFoundError}

import org.json4s._
import org.json4s.JsonDSL._

trait MeApi extends ServiceBase {

  val userDao: UserDAO

//  def getUser(cid: UserId): Directive1[User] = provide((userDao get cid) getOrElse { throw UserNotFoundError(cid) })

  private val getUser = (uid: UserId) => userDao get uid getOrElse { throw UserNotFoundError(uid) }

  private def completeWithUserSetCheck(uid: UserId)(field: User => Set[String])(value: String): Route =
    complete {
      Map("contains" -> {
        uid |> { getUser andThen field andThen { _ contains value} }
      })
    }


  private val getFieldsRoute: CallerRoute = cid => get {
    path(PathEnd) {
      complete { 
        getUser(cid)
      }
    } ~ path(User.ID / PathEnd) {
      complete { 
        Map(User.ID -> getUser(cid).id)
      }
    } ~ path(User.NICK / PathEnd) {
      complete { 
        Map(User.NICK -> getUser(cid).nick)
      }
    }~ path(User.PUBLIC / PathEnd) {
      complete {
        Map(User.PUBLIC -> getUser(cid).public)
      }
    } ~ path(User.INFO / PathEnd) {
      complete {
        Map(User.INFO -> getUser(cid).info)
      }
    } ~ path(User.FOLLOWING / PathEnd) {
      complete {
        Map(User.FOLLOWING -> getUser(cid).following)
      }
    } ~ path(User.FOLLOWERS / PathEnd) {
      complete {
        Map(User.FOLLOWERS -> getUser(cid).followers)
      }
    } ~ path(User.BLOCKED / PathEnd) {
      complete {
        Map(User.BLOCKED -> getUser(cid).blocked)
      }
    } ~ path(User.TOPICS / PathEnd) {
      complete {
        Map(User.TOPICS -> getUser(cid).topics)
      }
    } ~ path(User.TAGS / PathEnd) {
      complete {
        Map(User.TAGS -> getUser(cid).tags)
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
    decodeRequest(NoEncoding) {
      path(User.NICK / PathEnd) {
        entity(as[String]) { v =>
          complete {
            userDao.setNick(cid, v)
          }
        }
      } ~ path(User.PUBLIC / PathEnd) {
        entity(fromString[Boolean]) { v =>
          complete {
            userDao.setPublic(cid, v)
          }
        }
      } ~ path(User.INFO / PathEnd) {
        optionJsonEntity {
          case Some(m) => complete {
            userDao.setInfo(cid, m)
          }
          case None => complete {
            400 -> "need a json object here"
          }
        }
      }
    }
  }

  private val addToSets: CallerRoute = cid => put {
    path(User.FOLLOWING / Segment / PathEnd) { u: UserId =>
      optionJsonEntity { oj =>
        complete {
          StringR("whatever")
        }
      }
    } ~ path(User.BLOCKED / Segment / PathEnd) { u: UserId =>
      complete {
        "whatever"
      }
    } ~ path(User.TOPICS / Segment / PathEnd) { t: TopicId =>
      optionJsonEntity { m: Option[JDoc] =>
        complete {
          "whatever"
        }
      }
    } ~ path(User.TAGS / Segment / PathEnd) { t: String =>
      complete {
        "whatever"
      }
    }
  }

  private val deleteFromSets: CallerRoute = cid => delete {
    path(User.FOLLOWING / Segment / PathEnd) { u: UserId =>
      complete {
        "whatever"
      }
    } ~ path(User.FOLLOWERS / Segment / PathEnd) { u: UserId =>
      complete {
        "whatever"
      }
    } ~ path(User.BLOCKED / Segment / PathEnd) { u: UserId =>
      complete {
        "whatever"
      }
    } ~ path(User.TOPICS / Segment / PathEnd) { t: TopicId =>
      complete {
        "whatever"
      }
    } ~ path(User.TAGS / Segment / PathEnd) { t: String =>
      complete {
        "whatever"
      }
    }
  }


  private val cr: CallerRoute = getFieldsRoute |+| querySetsRoute |+| addToSets |+| deleteFromSets

  val meApi: CallerRoute = cid => resJson {
    pathPrefix(ME_API_BASE) {
      getFieldsRoute(cid) ~ querySetsRoute(cid) ~ replaceFields(cid) ~ addToSets(cid) ~ deleteFromSets(cid)
    }
  }
}
