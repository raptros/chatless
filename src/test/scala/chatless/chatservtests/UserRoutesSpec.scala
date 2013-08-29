package chatless.chatservtests

import org.scalatest.WordSpec
import spray.testkit.ScalatestRouteTest
import spray.http.StatusCodes
import chatless._
import org.scalatest.matchers.ShouldMatchers
import org.scalamock.scalatest.MockFactory
import chatless.models.{User, UserDAO}
import spray.routing.{HttpService, Directives}
import chatless.services.clientApi.UserApi
import org.json4s._
import org.json4s.native.JsonMethods._
import shapeless._

class UserRoutesSpec extends WordSpec with ServiceSpecBase with ScalatestRouteTest with ShouldMatchers with MockFactory {
  import User.{allFields, callerOnlyFields, publicFields, nonPublicFields, followerFields}

  val id1 = "00012"
  val id2 = "67000"
  val id3 = "33333"

  def extractJObj(oj: Option[JValue]): Option[JObject] = oj flatMap {
    case jo: JObject => Some(jo)
    case _ => None
  }

  def getFields(oj: Option[JObject]): Set[String] = oj map { _.values.keys.toSet } getOrElse Set.empty[String]

  val fakeCaller = User(
    userId,
    "this user",
    true,
    Map.empty[String, Any],
    Set(id2),
    Set("otherUser"),
    Set("some-blocked"),
    Set("tid0"),
    Set("tag0"))

  val otherUser1 = User(
    id1,
    "a public user",
    true,
    Map.empty[String, Any],
    Set("otherUser"),
    Set(userId),
    Set("some-blocked"),
    Set("tid0"),
    Set("tag0"))

  val otherUser2 = User(
    id2,
    "a private user followed",
    false,
    Map.empty[String, Any],
    Set(otherUser1.uid),
    Set(fakeCaller.uid),
    Set.empty[UserId],
    Set(),
    Set())

  val otherUser3 = User(
    id3,
    "a private user not followed",
    false,
    Map.empty[String, Any],
    Set(otherUser1.uid),
    Set(),
    Set.empty[UserId],
    Set(),
    Set())

  def mkPath(uid: UserId, field: String) = s"/user/$uid/$field"

  class Fixture(targetOther: User, count: Int) { self =>
    def mkGet(field: String = "") = Get(mkPath(targetOther.uid, field))
    val userDao = mock[UserDAO]
    (userDao.get(_: UserId)) expects targetOther.uid repeated count returning Some(targetOther)
    val userApi = new UserApi {
      val userDao = self.userDao
      override val actorRefFactory = system
    }
    val api = Directives.dynamic { userApi.userApi(userId) }
  }
  
  "the /user/ api" when {
    "the requested user is the caller" should {
      "return every field for the object" in new Fixture(fakeCaller, 1) {
        mkGet() ~> api ~> check {
          val oObj = extractJObj(parseOpt(entityAs[String]))
          assert(oObj.nonEmpty)
          val objFields = getFields(oObj)
          for (f <- allFields) {
            objFields should contain (f)
          }
        }
      }
      "return each field by request" in new Fixture(fakeCaller, allFields.length) {
        for (f <- allFields) {
          mkGet(f) ~> api ~> check {
            entity should not be ('isEmpty)
          }
        }
      }
    }
    "the requested user is public" should {
      "return every follower-visible field for the object" in new Fixture(otherUser1, 1) {
        mkGet() ~> api ~> check {
          val oObj = extractJObj(parseOpt(entityAs[String]))
          assert(oObj.nonEmpty)
          val objFields = getFields(oObj)
          for (f <- followerFields) {
            objFields should contain (f)
          }
        }
      }
      "not return any non-follower-visible fields in the object" in new Fixture(otherUser1, 1) {
        mkGet() ~> api ~> check {
          val oObj = extractJObj(parseOpt(entityAs[String]))
          assert(oObj.nonEmpty)
          val objFields = getFields(oObj)
          for (f <- callerOnlyFields) {
            objFields should not contain (f)
          }
        }
      }
      "return each field by request" in new Fixture(otherUser1, followerFields.length) {
        for (f <- followerFields) {
          mkGet(f) ~> api ~> check {
            entity should not be ('isEmpty)
          }
        }
      }
      "not allow requests to the caller-only fields" in new Fixture(otherUser1, callerOnlyFields.size) {
        for (f <- callerOnlyFields) {
          mkGet(f) ~> HttpService.sealRoute(api) ~> check {
            status === StatusCodes.Forbidden
          }
        }
      }
    }
    "the requested user is followed by the caller" should {
      "return every follower-visible field for the object" in new Fixture(otherUser2, 1) {
        mkGet() ~> api ~> check {
          val oObj = extractJObj(parseOpt(entityAs[String]))
          assert(oObj.nonEmpty)
          val objFields = getFields(oObj)
          for (f <- followerFields) {
            objFields should contain (f)
          }
        }
      }
      "not return any non-follower-visible fields in the object" in new Fixture(otherUser2, 1) {
        mkGet() ~> api ~> check {
          val oObj = extractJObj(parseOpt(entityAs[String]))
          assert(oObj.nonEmpty)
          val objFields = getFields(oObj)
          for (f <- callerOnlyFields) {
            objFields should not contain (f)
          }
        }
      }
      "return each field by request" in new Fixture(otherUser2, followerFields.length) {
        for (f <- followerFields) {
          mkGet(f) ~> api ~> check {
            entity should not be ('isEmpty)
          }
        }
      }
      "not allow requests to the caller-only fields" in new Fixture(otherUser2, callerOnlyFields.size) {
        for (f <- callerOnlyFields) {
          mkGet(f) ~> HttpService.sealRoute(api) ~> check {
            status === StatusCodes.Forbidden
          }
        }
      }
    }
    "the requested user is not public and not followed" should {
      "return every publically visible field for the object" in new Fixture(otherUser3, 1) {
        mkGet() ~> api ~> check {
          val oObj = extractJObj(parseOpt(entityAs[String]))
          assert(oObj.nonEmpty)
          val objFields = getFields(oObj)
          for (f <- publicFields) {
            objFields should contain (f)
          }
        }
      }
      "not return any non-publically-visible fields in the object" in new Fixture(otherUser3, 1) {
        mkGet() ~> api ~> check {
          val oObj = extractJObj(parseOpt(entityAs[String]))
          assert(oObj.nonEmpty)
          val objFields = getFields(oObj)
          for (f <- nonPublicFields) {
            objFields should not contain (f)
          }
        }
      }
      "return each publically visible field by request" in new Fixture(otherUser3, publicFields.length) {
        for (f <- publicFields) {
          mkGet(f) ~> api ~> check {
            entity should not be ('isEmpty)
          }
        }
      }
      "not allow requests to the caller-only fields" in new Fixture(otherUser3, nonPublicFields.size) {
        for (f <- nonPublicFields) {
          mkGet(f) ~> HttpService.sealRoute(api) ~> check {
            status === StatusCodes.Forbidden
          }
        }
      }
    }
  }

}
