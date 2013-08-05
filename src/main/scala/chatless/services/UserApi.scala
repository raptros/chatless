package chatless.services

import spray.routing._
import HListDeserializer._

import shapeless._

import chatless.UserId
import chatless.operation._
import chatless.op2.UserM

trait UserApi extends ServiceBase {

  def protectedUserCompletions(cid:UserId, user:UserM):Route = authorize(user.public || (user.followers contains cid)) {
    path("info" / PathEnd) {
      completeJson(user.info)
    } ~ path("following" / PathEnd) {
      completeJson(user.following)
    } ~ path("followers" / PathEnd) {
      completeJson(user.followers)
    } ~ path("topics" / PathEnd) {
      completeJson(user.topics)
    }
  } ~ authorize(user.uid == cid) {
    path("blocked" / PathEnd) {
      completeJson(user.blocked)
    } ~ path("tags" / PathEnd) {
      completeJson(user.tags)
    }
  }

  def userCompletions(cid:UserId)(user:UserM):Route =
    path(PathEnd) {
      completeJson(user)
    } ~ path("uid" / PathEnd) {
      completeString(user.uid)
    } ~ path("nick" / PathEnd) {
      completeString(user.nick)
    } ~ path("public" / PathEnd) {
      completeBoolean(user.public)
    } ~ protectedUserCompletions(cid, user)

  def userApi(cid:UserId) = get {
    pathPrefix("user" / Segment) { uid:UserId =>
      onSuccess(dbac.getUser(cid, uid)) { userCompletions(cid) }
    }
  }
}
