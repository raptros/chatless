package chatless.services.clientApi

import spray.routing._
import chatless._
import chatless.services._
import chatless.model._
import chatless.db.UserDAO
import chatless.responses.UserNotFoundError
import org.json4s._
import org.json4s.JsonDSL._
import chatless.ops.UserOps


trait UserApi extends ServiceBase {

  val userOps: UserOps

  private def userFieldsSelector(cid: UserId, user: => User): Set[String] =
    if (user.id == cid)
      User.allFields
    else if (user.public || (user.followers contains cid))
      User.followerFields
    else
      User.publicFields

  val userApi: CallerRoute = cid => get {
    pathPrefix(USER_API_BASE / Segment) map userOps.getOrThrow apply { user =>
    /*these paths can be requested by any user*/
      pathEnd {
        resJson { complete { user getFields userFieldsSelector(cid, user) } }
      } ~ completeFieldsAs(
        User.ID     -> user.id,
        User.NICK   -> user.nick,
        User.PUBLIC -> user.public
      ) ~ authorize(check = user.public || (user.followers contains cid) || (user.id == cid)) {
        /*if the user is public, is followed by the caller, or is the caller, the caller can read these*/
        completeFieldsAs(
          User.INFO      -> user.info,
          User.FOLLOWING -> user.following,
          User.FOLLOWERS -> user.followers,
          User.TOPICS    -> user.topics
        ) ~ completeWithContains(
          User.FOLLOWING -> user.following,
          User.FOLLOWERS -> user.followers,
          User.TOPICS    -> user.topics)
      } ~ authorize(user.id == cid) {
        /*only the user requested can read these fields*/
        completeFieldsAs(
          User.BLOCKED -> user.blocked,
          User.TAGS    -> user.tags
        ) ~ completeWithContains(
          User.BLOCKED -> user.blocked,
          User.TAGS    -> user.tags)
      }
    }
  }
}
