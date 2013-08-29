package chatless.services.clientApi

import spray.routing._


import chatless.UserId

import chatless.services._
import chatless.models.{User, UserDAO}
import chatless.db.UserNotFoundError
import shapeless._
import Typeable._
import org.json4s._

trait UserApi extends ServiceBase with UserMethods {

  val USER_API_BASE = "user"


  private def protectedUserCompletions(cid: UserId, user: User): Route =
    authorize(user.public || (user.followers contains cid)) {
      path(User.INFO / PathEnd) {
        resJson { complete { user.info } }
      } ~ path(User.FOLLOWING / PathEnd) {
        resJson { complete { user.following } }
      } ~ path(User.FOLLOWERS / PathEnd) {
        resJson { complete { user.followers } }
      } ~ path(User.TOPICS / PathEnd) {
        resJson { complete { user.topics } }
      }
    } ~ authorize(user.uid == cid) {
      path(User.BLOCKED / PathEnd) {
        resJson { complete { user.blocked } }
      } ~ path(User.TAGS / PathEnd) {
        resJson { complete { user.tags } }
      }
    }

  private def userFieldsSelector(cid: UserId, user: User): Set[String] =
    if (user.uid == cid)
      User.allFields
    else if (user.public || (user.followers contains cid))
      User.followerFields
    else
      User.publicFields

  def userJsonFields(user: User, fields: Set[String]): JValue


  private def userCompletions(cid: UserId)(user: User): Route = {
    val fields = userFieldsSelector(cid, user)
    val filteredUser = mapUser(user) filterKeys { fields.contains }
    val setFields = for {
      (k, v) <- filteredUser
      vC <- v.cast[Set[String]]
    } yield k -> vC

    path(PathEnd) {
      resJson { complete { filteredUser } }
    } ~

    path(filteredUser / PathEnd) { f: Any =>
      complete { mkRes(f) }
    } ~ path(setFields / Segment / PathEnd) { (set, id) =>
      resText { complete { mkRes(set contains id) } }
    }
  }

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
