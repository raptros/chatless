package chatless.chatservtests

import org.scalatest.WordSpec
import spray.testkit.ScalatestRouteTest
import spray.http.StatusCodes
import chatless._
import org.scalatest.matchers.ShouldMatchers
import org.scalamock.scalatest.MockFactory
import chatless.models.UserM
import chatless.db.DatabaseAccessor
import scala.concurrent.Future
import chatless.services.UserApi
import argonaut._
import Argonaut._
import spray.routing.{HttpService, Directives}

class UserRoutesSpec extends WordSpec with ServiceSpecBase with ScalatestRouteTest with ShouldMatchers with MockFactory {
  import UserM.{allFields, callerOnlyFields, publicFields, nonPublicFields, followerFields}

  val id1 = "00012"
  val id2 = "67000"
  val id3 = "33333"

  val fakeCaller = UserM(
    userId,
    "this user",
    true,
    jEmptyObject,
    Set(id2),
    Set("otherUser"),
    Set("some-blocked"),
    Set("tid0"),
    Set("tag0"))

  val otherUser1 = UserM(
    id1,
    "a public user",
    true,
    jEmptyObject,
    Set("otherUser"),
    Set(userId),
    Set("some-blocked"),
    Set("tid0"),
    Set("tag0"))

  val otherUser2 = UserM(
    id2,
    "a private user followed",
    false,
    jEmptyObject,
    Set(otherUser1.uid),
    Set(fakeCaller.uid),
    Set.empty[UserId],
    Set(),
    Set())

  val otherUser3 = UserM(
    id3,
    "a private user not followed",
    false,
    jEmptyObject,
    Set(otherUser1.uid),
    Set(),
    Set.empty[UserId],
    Set(),
    Set())

  def mkPath(uid: UserId, field: String) = s"/user/$uid/$field"

  class Fixture(targetOther: UserM, count: Int) {
    def mkGet(field: String = "") = Get(mkPath(targetOther.uid, field))
    val dbac = mock[DatabaseAccessor]
    (dbac.getUser _) expects(userId, targetOther.uid) repeated count returning Future.successful(targetOther)
    val route = new UserApi(dbac)
    val api = Directives.dynamic { route(userId) }
  }
  
  "the /user/ api" when {
    "the requested user is the caller" should {
      "return every field for the object" in new Fixture(fakeCaller, 1) {
        mkGet() ~> api ~> check {
          val oObj = entityAs[String].parseOption
          assert(oObj.nonEmpty)
          val objFields = oObj flatMap { _.objectFields } getOrElse Nil
          allFields foreach { f =>
            objFields should contain (f)
          }
        }
      }
      "return each field by request" in new Fixture(fakeCaller, allFields.length) {
        allFields foreach { f =>
          mkGet(f) ~> api ~> check {
            entity should not be ('isEmpty)
          }
        }
      }
    }
    "the requested user is public" should {
      "return every follower-visible field for the object" in new Fixture(otherUser1, 1) {
        mkGet() ~> api ~> check {
          val oObj = entityAs[String].parseOption
          assert(oObj.nonEmpty)
          val objFields = oObj flatMap { _.objectFields } getOrElse Nil
          followerFields foreach { f =>
            objFields should contain (f)
          }
        }
      }
      "not return any non-follower-visible fields in the object" in new Fixture(otherUser1, 1) {
        mkGet() ~> api ~> check {
          val oObj = entityAs[String].parseOption
          assert(oObj.nonEmpty)
          val objFields = oObj flatMap { _.objectFields } getOrElse Nil
          callerOnlyFields foreach { f =>
            objFields should not contain (f)
          }
        }
      }
      "return each field by request" in new Fixture(otherUser1, followerFields.length) {
        followerFields foreach { f =>
          mkGet(f) ~> api ~> check {
            entity should not be ('isEmpty)
          }
        }
      }
      "not allow requests to the caller-only fields" in new Fixture(otherUser1, callerOnlyFields.size) {
        callerOnlyFields foreach { f =>
          mkGet(f) ~> HttpService.sealRoute(api) ~> check {
            status === StatusCodes.Forbidden
          }
        }
      }
    }
    "the requested user is followed by the caller" should {
      "return every follower-visible field for the object" in new Fixture(otherUser2, 1) {
        mkGet() ~> api ~> check {
          val oObj = entityAs[String].parseOption
          assert(oObj.nonEmpty)
          val objFields = oObj flatMap { _.objectFields } getOrElse Nil
          followerFields foreach { f =>
            objFields should contain (f)
          }
        }
      }
      "not return any non-follower-visible fields in the object" in new Fixture(otherUser2, 1) {
        mkGet() ~> api ~> check {
          val oObj = entityAs[String].parseOption
          assert(oObj.nonEmpty)
          val objFields = oObj flatMap { _.objectFields } getOrElse Nil
          callerOnlyFields foreach { f =>
            objFields should not contain (f)
          }
        }
      }
      "return each field by request" in new Fixture(otherUser2, followerFields.length) {
        followerFields foreach { f =>
          mkGet(f) ~> api ~> check {
            entity should not be ('isEmpty)
          }
        }
      }
      "not allow requests to the caller-only fields" in new Fixture(otherUser2, callerOnlyFields.size) {
        callerOnlyFields foreach { f =>
          mkGet(f) ~> HttpService.sealRoute(api) ~> check {
            status === StatusCodes.Forbidden
          }
        }
      }
    }
    "the requested user is not public and not followed" should {
      "return every publically visible field for the object" in new Fixture(otherUser3, 1) {
        mkGet() ~> api ~> check {
          val oObj = entityAs[String].parseOption
          assert(oObj.nonEmpty)
          val objFields = oObj flatMap { _.objectFields } getOrElse Nil
          publicFields foreach { f =>
            objFields should contain (f)
          }
        }
      }
      "not return any non-publically-visible fields in the object" in new Fixture(otherUser3, 1) {
        mkGet() ~> api ~> check {
          val oObj = entityAs[String].parseOption
          assert(oObj.nonEmpty)
          val objFields = oObj flatMap { _.objectFields } getOrElse Nil
          nonPublicFields foreach { f =>
            objFields should not contain (f)
          }
        }
      }
      "return each publically visible field by request" in new Fixture(otherUser3, publicFields.length) {
        publicFields foreach { f =>
          mkGet(f) ~> api ~> check {
            entity should not be ('isEmpty)
          }
        }
      }
      "not allow requests to the caller-only fields" in new Fixture(otherUser3, nonPublicFields.size) {
        nonPublicFields foreach { f =>
          mkGet(f) ~> HttpService.sealRoute(api) ~> check {
            status === StatusCodes.Forbidden
          }
        }
      }
    }
  }

}
