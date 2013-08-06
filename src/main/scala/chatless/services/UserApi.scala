package chatless.services

import spray.routing._
import HListDeserializer._

import shapeless._

import chatless.UserId
import chatless.op2.UserM

import scalaz.std.function._
import scalaz.syntax.semigroup._

trait UserApi extends ServiceBase {
  val USER_API_BASE = "user"

  def protectedUserCompletions(cid:UserId, user:UserM):Route = authorize(user.public || (user.followers contains cid)) {
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

  def userCompletions(cid:UserId)(user:UserM):Route =
    path(PathEnd) {
      completeJson(user)
    } ~ path(UserM.UID / PathEnd) {
      completeString(user.uid)
    } ~ path(UserM.NICK / PathEnd) {
      completeString(user.nick)
    } ~ path(UserM.PUBLIC / PathEnd) {
      completeBoolean(user.public)
    } ~ protectedUserCompletions(cid, user)

  def userApi(cid:UserId) = get {
    pathPrefix(USER_API_BASE / Segment) { uid:UserId =>
      onSuccess(dbac.getUser(cid, uid)) {
        userCompletions(cid)
      }
    }
  }
}
