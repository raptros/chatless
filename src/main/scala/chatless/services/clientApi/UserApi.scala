package chatless.services.clientApi

import spray.routing._


import chatless._
import chatless.services._
import chatless.model._
import chatless.responses.{BoolR, StringR, UserNotFoundError}
import chatless.db.UserDAO

trait UserApi extends ServiceBase {

  val USER_API_BASE = "user"

  val userDao: UserDAO

//  private def protectedUserCompletions(cid: UserId, user: User): Route =

  private def userFieldsSelector(cid: UserId, user: => User): Set[String] =
    if (user.uid == cid)
      User.allFields
    else if (user.public || (user.followers contains cid))
      User.followerFields
    else
      User.publicFields



  private def followerRoutes(user: User): Route =
    path(User.INFO / PathEnd) {
      complete { user.info }
    } ~ path(User.FOLLOWING / PathEnd) {
      complete { user.following }
    } ~ path(User.FOLLOWERS / PathEnd) {
      complete { user.followers }
    } ~ path(User.TOPICS / PathEnd) {
      complete { user.topics }
    } ~ setCompletion(
      User.FOLLOWING -> user.following,
      User.FOLLOWERS -> user.followers,
      User.TOPICS -> user.topics)

  private def callerRoutes(user: User): Route =
    path(User.BLOCKED / PathEnd) {
      complete { user.blocked }
    } ~ path(User.TAGS / PathEnd) {
      complete { user.tags }
    } ~ setCompletion(
      User.BLOCKED -> user.blocked,
      User.TAGS -> user.tags)

  private def publicRoutes(user: User)(fields: => Set[String]): Route =
    path(PathEnd) {
      complete { user getFields fields }
    } ~ path(User.UID / PathEnd) {
      complete(StringR(user.uid))
    } ~ path(User.NICK / PathEnd) {
      complete(StringR(user.nick))
    } ~ path(User.PUBLIC / PathEnd) {
      complete(BoolR(user.public))
    }

  private def userGets(cid: UserId)(user: User): Route =
    publicRoutes(user) {
      userFieldsSelector(cid, user)
    } ~ authorize(user.public || (user.followers contains cid)) {
      followerRoutes(user)
    } ~ authorize(user.uid == cid) {
      callerRoutes(user)
    }


  val userApi: CallerRoute = cid => get {
    resJson {
      pathPrefix(USER_API_BASE / Segment) { uid: UserId =>
        userGets(cid) {
          userDao get uid getOrElse {
            throw UserNotFoundError(uid)
          }
        }
      }
    }
  }
}
