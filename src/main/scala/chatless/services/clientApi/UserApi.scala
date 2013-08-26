package chatless.services.clientApi

import spray.routing._


import chatless.UserId

import chatless.services._
import chatless.models.{User, UserDAO}
import chatless.db.UserNotFoundError

trait UserApi extends ServiceBase {

  val USER_API_BASE = "user"

  val userDao: UserDAO

  private def protectedUserCompletions(cid: UserId, user: User): Route = authorize(user.public || (user.followers contains cid)) {
    path(User.INFO / PathEnd) {
      completeJson(user.info)
    } ~ path(User.FOLLOWING / PathEnd) {
      completeJson(user.following)
    } ~ path(User.FOLLOWERS / PathEnd) {
      completeJson(user.followers)
    } ~ path(User.TOPICS / PathEnd) {
      completeJson(user.topics)
    }
  } ~ authorize(user.uid == cid) {
    path(User.BLOCKED / PathEnd) {
      completeJson(user.blocked)
    } ~ path(User.TAGS / PathEnd) {
      completeJson(user.tags)
    }
  }

  private def userFieldsSelector(cid: UserId, user: User): List[String] =
    if (user.uid == cid)
      User.allFields
    else if (user.public || (user.followers contains cid))
      User.followerFields
    else
      User.publicFields


  private def userCompletions(cid: UserId)(user: User): Route =
    path(PathEnd) {
      completeJson { filterJson(user.asJson, userFieldsSelector(cid, user)) }
    } ~ path(User.UID / PathEnd) {
      completeString(user.uid)
    } ~ path(User.NICK / PathEnd) {
      completeString(user.nick)
    } ~ path(User.PUBLIC / PathEnd) {
      completeBoolean(user.public)
    } ~ protectedUserCompletions(cid, user)

  val userApi: CallerRoute = cid => get {
    pathPrefix(USER_API_BASE / Segment) { uid: UserId =>
      userCompletions(cid) {
        userDao get cid getOrElse {
          throw UserNotFoundError(cid)
        }
      }
    }
  }
}
