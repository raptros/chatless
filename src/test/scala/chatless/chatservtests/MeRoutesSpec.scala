package chatless.chatservtests

import org.scalatest.WordSpec

import spray.testkit.ScalatestRouteTest
import spray.routing._
import spray.http.StatusCodes._
import chatless.services.MeApi
import chatless.db.{DatabaseAccessor, DatabaseActorClient}
import scala.concurrent._
import chatless._
import chatless.op2._
import spray.http.BasicHttpCredentials
import spray.routing.authentication.{UserPass, UserPassAuthenticator, BasicAuth, ContextAuthenticator}
import argonaut._
import Argonaut._
import scalaz.syntax.std.option._

import org.scalatest.matchers.ShouldMatchers
import org.scalamock.scalatest.MockFactory
import chatless.models.UserM

class MeRoutesSpec
  extends WordSpec
  with ScalatestRouteTest
  with ServiceSpecBase
  with ShouldMatchers
  with MockFactory {
  val fakeUser1 = UserM(userId, "this user", true, jEmptyObject,
    Set("otherUser"), Set("otherUser"), Set("some-blocked"),
    Set("tid0"), Set("tag0"))


  trait Fixture1 {
    val dbac = mock[DatabaseAccessor]
    (dbac.getUser _) expects(userId, userId) returning Future.successful(fakeUser1)
    val route = new MeApi(dbac)
    val api = Directives.dynamic { route(userId) }
  }

  class Fixture2 (val spec: UpdateSpec with ForUsers) {
    val dbac = mock[DatabaseAccessor]
    val route = new MeApi(dbac)
    val api = Directives.dynamic { route(userId) }
    (dbac.getUser _) expects(*, *) never()
    (dbac.updateUser _) expects(userId, userId, spec) once() returning Future.successful(true)
  }

  def itReceives = afterWord("it receives")
  def theFieldIs = afterWord("the field is")

  "the me api" should {
    "provide the correct user object, deserialzable from json" when itReceives {
      "a get to its base" in new Fixture1 {
        Get("/me") ~>  api ~> check {
          val res = entityAs[String].decodeOption[UserM]
          res should be (fakeUser1.some)
        }
      }
    }
    "produce the correct value" when itReceives {
      "a get for the uid field" in new Fixture1 {
        Get("/me/uid") ~> api ~> check {
          val res = entityAs[String]
          assert(res === userId)
        }
      }
      "a get for the nick field" in new Fixture1 {
        Get("/me/nick") ~> api ~> check {
          val res = entityAs[String]
          assert(res === "this user")
        }
      }
      "a get for the public field" in new Fixture1 {
        Get("/me/public") ~> api ~> check {
          val res = entityAs[Boolean]
          assert(res === fakeUser1.public)
        }
      }
      "a get for the info field" in new Fixture1 {
        Get("/me/info") ~>  api ~> check {
          val res = entityAs[String].parseOption.get
          res should equal (fakeUser1.info)
        }
      }
      "a get for the following field" in new Fixture1 {
        Get("/me/following") ~>  api ~> check {
          val res = entityAs[String].decodeOption[Set[UserId]].get
          res should equal (fakeUser1.following)
        }
      }
      "a get for the followers field" in new Fixture1 {
        Get("/me/followers") ~>  api ~> check {
          val res = entityAs[String].decodeOption[Set[UserId]].get
          res should equal (fakeUser1.followers)
        }
      }
      "a get for the blocked field " in new Fixture1 {
        Get("/me/blocked") ~>  api ~> check {
          val res = entityAs[String].decodeOption[Set[UserId]].get
          res should equal (fakeUser1.blocked)
        }
      }
      "a get for the topics field" in new Fixture1 {
        Get("/me/topics") ~>  api ~> check {
          val res = entityAs[String].decodeOption[Set[TopicId]].get
          res should equal (fakeUser1.topics)
        }
      }
      "a get for the tags field " in new Fixture1 {
        Get("/me/tags") ~>  api ~> check {
          val res = entityAs[String].decodeOption[Set[TopicId]].get
          res should equal (fakeUser1.tags)
        }
      }
      "a query: following contains a user that is followed" in new Fixture1 {
        Get("/me/following/otherUser") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
      }
      "a query: following contains a user that is not followed" in new Fixture1 {
        Get("/me/following/fakeUser") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
      }
      "a query: followers contains a user that is following" in new Fixture1 {
        Get("/me/followers/otherUser") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
      }
      "a query: followers contains a user that is not following" in new Fixture1 {
        Get("/me/followers/fakeUser") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
      }
      "a query: blocked contains a user that is blocked" in new Fixture1 {
        Get("/me/blocked/some-blocked") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
      }
      "a query: blocked contains a user that is not blocked" in new Fixture1 {
        Get("/me/blocked/fakeUser") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
      }
      "a query: topics contains a topic that user does participate in" in new Fixture1 {
        Get("/me/topics/tid0") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
      }
      "a query: topics contains a topic that user does not participate in" in new Fixture1 {
        Get("/me/topics/fakeTopic") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
      }
      "a query: tags contains a tracked tag" in new Fixture1 {
        Get("/me/tags/tag0") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
      }
      "a query: tags contains an untracked tagged" in new Fixture1 {
        Get("/me/tags/fakeTag") ~>  api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
      }
    }
    "hit the database accessor properly" when itReceives {
      "a PUT to /me/nick" in new Fixture2(ReplaceNick("wark")) {
        Put("/me/nick", "wark") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/public" in new Fixture2(SetPublic(false)) {
        Put("/me/public", false) ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/info" in new Fixture2(UpdateInfo(("place" := "testing") ->: jEmptyObject)) {
        Put("/me/info", spec.asInstanceOf[UpdateInfo].info.nospaces) ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/following/:uid" in new Fixture2(FollowUser("user2")) {
        Put("/me/following/user2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/following/:uid with a payload" in new Fixture2(FollowUser("user2", Some(("place" := "testing") ->: jEmptyObject))) {
        Put("/me/following/user2", spec.asInstanceOf[FollowUser].additional.get.nospaces) ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a delete to /me/following/:uid" in new Fixture2(UnfollowUser("user2")) {
        Delete("/me/following/user2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a delete to /me/followers/:uid" in new Fixture2(RemoveFollower("user2")) {
        Delete("/me/followers/user2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/blocked/:uid" in new Fixture2(BlockUser("someuser2")) {
        Put("/me/blocked/someuser2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a delete to /me/blocked/:uid" in new Fixture2(UnblockUser("someuser2")) {
        Delete("/me/blocked/someuser2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/topics/:tid" in new Fixture2(JoinTopic("topic2")) {
        Put("/me/topics/topic2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/topics/:tid with a payload" in new Fixture2(JoinTopic("topic2", Some(("place" := "testing") ->: jEmptyObject))) {
        Put("/me/topics/topic2", spec.asInstanceOf[JoinTopic].additional.get.nospaces) ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a delete to /me/topics/:tid" in new Fixture2(LeaveTopic("topic2")) {
        Delete("/me/topics/topic2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/tags/:tag" in new Fixture2(AddTag("tag2")) {
        Put("/me/tags/tag2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a delete to /me/tags/:tag" in new Fixture2(RemoveTag("tag2")) {
        Delete("/me/tags/tag2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
    }
  }
}