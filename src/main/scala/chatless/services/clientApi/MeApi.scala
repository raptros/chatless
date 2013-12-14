package chatless.services.clientApi

import chatless._
import chatless.db._
import chatless.services.routeutils._


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
import shapeless.HNil

trait MeApi extends ServiceBase {

  val userOps: UserOps

  def setContains[A](v: A): Set[A] => Boolean = _ contains v

  val mkJObj: Pair[String, JValue] => JValue = p => JObject(p)

  private def mapExtractors(extractors: Map[String, User => JValue]) = extractors map {
    case (field, extractFunc) => field -> { extractFunc andThen field.-> andThen mkJObj }
  }

  private def queryFields(extractors: (String, User => JValue)*): CallerRoute = cid => {
    fPath(mapExtractors(extractors.toMap)) { extractor =>
      resJson { complete(extractor(userOps.getOrThrow(cid))) }
    }
  }

  def setChecks(setFields: (String, User => Set[String])*): CallerRoute = cid =>
    fPath(setFields.toMap / Segment) { (e, v) => complete(check(cid, e, v)) }

  private def check(cid: UserId, extractor: User => Set[String], value: String) =
    if (cid |> userOps.getOrThrow |> extractor |> setContains(value)) StatusCodes.NoContent else StatusCodes.NotFound

  private val renderUser: CallerRoute = cid => (pathEndOrSingleSlash & resJson) { complete(userOps.getOrThrow(cid)) }

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

  private def allPuts(cid: UserId) = routeCarriers(
    User.NICK      carry sEntity(fromString[String]) buildOp { userOps.setNick(cid, _) },
    User.PUBLIC    carry sEntity(fromString[Boolean]) buildOp { userOps.setPublic(cid, _) },
    User.INFO      carry sEntity(as[JObject]) buildOp { j => userOps.setInfo(cid, JDoc(j.obj)) },
    //todo build the request system to handle follow requests
    User.FOLLOWING carry (segFin & optionJsonEntity) buildOp { (uid, oj) => userOps.followUser(cid, uid)},
    User.BLOCKED   carry segFin buildOp { userOps.blockUser(cid, _) },
    //todo see above
    User.TOPICS    carry (segFin & optionJsonEntity) buildOp { (tid, oj) => userOps.joinTopic(cid, tid) },
    User.TAGS      carry segFin buildOp { userOps.addTag(cid, _) }
  )

  private def allDeletes(cid: UserId) = routeCarriers(
    User.FOLLOWING carry segFin buildOp { userOps.unfollowUser(cid, _) },
    User.FOLLOWERS carry segFin buildOp { userOps.removeFollower(cid, _) },
    User.BLOCKED   carry segFin buildOp { userOps.unblockUser(cid, _) },
    User.TOPICS    carry segFin buildOp { userOps.leaveTopic(cid, _) },
    User.TAGS      carry segFin buildOp { userOps.removeTag(cid, _) }
  )

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
