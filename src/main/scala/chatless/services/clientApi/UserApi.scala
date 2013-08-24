package chatless.services.clientApi

import spray.routing._
import HListDeserializer._

import argonaut._
import Argonaut._

import shapeless._

import chatless.UserId

import scalaz.std.function._
import scalaz.syntax.semigroup._
import chatless.models.UserM
import chatless.db.DatabaseAccessor
import akka.actor.ActorRefFactory
import scala.concurrent.ExecutionContext
import chatless.services._
import spray.routing.PathMatchers.PathEnd
import com.google.inject.Inject

trait UserApi extends ServiceBase {

  val USER_API_BASE = "user"

  private def protectedUserCompletions(cid: UserId, user: UserM): Route = authorize(user.public || (user.followers contains cid)) {
    path(UserM.INFO / PathEnd) {
      completeJson(user.info)
    } ~ path(UserM.FOLLOWING / PathEnd) {
      completeJson(user.following)
    } ~ path(UserM.FOLLOWERS / PathEnd) {
      completeJson(user.followers)
    } ~ path(UserM.TOPICS / PathEnd) {
      completeJson(user.topics)
    }
  } ~ authorize(user.uid == cid) {
    path(UserM.BLOCKED / PathEnd) {
      completeJson(user.blocked)
    } ~ path(UserM.TAGS / PathEnd) {
      completeJson(user.tags)
    }
  }

  private def userFieldsSelector(cid: UserId, user: UserM): List[String] = if (user.uid == cid)
    UserM.allFields
  else if (user.public || (user.followers contains cid))
    UserM.followerFields
  else
    UserM.publicFields

  private def userCompletions(cid: UserId)(user: UserM): Route =
    path(PathEnd) {
      completeJson { filterJson(user.asJson, userFieldsSelector(cid, user)) }
    } ~ path(UserM.UID / PathEnd) {
      completeString(user.uid)
    } ~ path(UserM.NICK / PathEnd) {
      completeString(user.nick)
    } ~ path(UserM.PUBLIC / PathEnd) {
      completeBoolean(user.public)
    } ~ protectedUserCompletions(cid, user)

  val userApi: CallerRoute = cid => get {
    pathPrefix(USER_API_BASE / Segment) { uid: UserId =>
      onSuccess(dbac.getUser(cid, uid)) {
        userCompletions(cid)
      }
    }
  }
}
