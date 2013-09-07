package chatless.services.clientApi

import spray.routing._
import chatless._
import chatless.services._
import chatless.model._
import chatless.db.UserDAO
import chatless.responses.UserNotFoundError
import org.json4s._
import org.json4s.JsonDSL._


trait UserApi extends ServiceBase {

  val userDao: UserDAO

  private def fieldComplete[A <% JValue](field: String)(value: A) = path(field / PathEnd) {
    complete { Map(field -> value) }
  }
  private def userFieldsSelector(cid: UserId, user: => User): Set[String] =
    if (user.id == cid)
      User.allFields
    else if (user.public || (user.followers contains cid))
      User.followerFields
    else
      User.publicFields


  val userApi: CallerRoute = cid => get {
    resJson {
      pathPrefix(USER_API_BASE / Segment) { uid: UserId =>
        val user = userDao get uid getOrElse { throw UserNotFoundError(uid) }
        /*----------------------------------------*/
        /*these paths can be requested by any user*/
        path(PathEnd) {
          complete { user getFields userFieldsSelector(cid, user) }
        } ~ fieldComplete(User.ID) {
          user.id
        } ~ fieldComplete(User.NICK) {
          user.nick
        } ~ fieldComplete(User.PUBLIC) {
          user.public
        } ~ authorize(check = user.public || (user.followers contains cid) || (user.id == cid)) {
          /*---------------------------------------------------------------------------------------------*/
          /*if the user is public, is followed by the caller, or is the caller, the caller can read these*/
          fieldComplete(User.INFO) {
            user.info
          } ~ fieldComplete(User.FOLLOWING) {
            user.following
          } ~ fieldComplete(User.FOLLOWERS) {
            user.followers
          } ~ fieldComplete(User.TOPICS) {
            user.topics
          } ~ setCompletion(
            User.FOLLOWING -> user.following,
            User.FOLLOWERS -> user.followers,
            User.TOPICS    -> user.topics)
          /*---------------------------------------------------------------------------------------------*/
        } ~ authorize(user.id == cid) {
          /*---------------------------------------------*/
          /*only the user requested can read these fields*/
          fieldComplete(User.BLOCKED) {
            user.blocked
          } ~ fieldComplete(User.TAGS) {
            user.tags
          } ~ setCompletion(
            User.BLOCKED -> user.blocked,
            User.TAGS    -> user.tags)
          /*---------------------------------------------*/
        }
      }
    }
  }
}
