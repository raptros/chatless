package chatless.chatservtests

import org.scalatest.WordSpec

import spray.testkit.ScalatestRouteTest
import spray.routing._
import chatless._
import chatless.op2._
import scalaz.syntax.std.option._

import org.scalatest.matchers.ShouldMatchers
import org.scalamock.scalatest.MockFactory
import chatless.db.UserDAO
import chatless.model.{JDoc, User}
import chatless.services.clientApi.MeApi
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import spray.httpx.Json4sSupport
import spray.httpx.unmarshalling.BasicUnmarshallers._
import chatless.responses.{BoolR, StringR}

class MeRoutesSpec
  extends WordSpec
  with ScalatestRouteTest
  with ServiceSpecBase
  with ShouldMatchers
  with MockFactory {


  val fakeUser1 = User(userId, "this user", true, JDoc("contact?" -> JBool(true)),
    Set("otherUser"), Set("otherUser"), Set("some-blocked"),
    Set("tid0"), Set("tag0"))

  def newApi(dao: UserDAO) = {
    val me = new MeApi {
      val userDao = dao
      val actorRefFactory = system
    }
    Directives.dynamic { me.meApi(userId) }
  }

  trait Fixture1 { self =>
    val userDao = mock[UserDAO]
    (userDao.get(_: UserId)) expects userId returning Some(fakeUser1)
    val api = newApi(userDao)
  }

  class Fixture2 (val spec: UpdateSpec with ForUsers) { self =>
    val dao = mock[UserDAO]
    val api = newApi(dao)
    (dao.get(_: UserId)) expects * never()
    //(dao.updateUser(_: UserId, _: UserId, _: UpdateSpec with ForUsers)) expects(userId, userId, spec, *) once() returning Future.successful(true)
  }

  def itReceives = afterWord("it receives")
  def theFieldIs = afterWord("the field is")

  "the me api" should {
    "provide the correct user object, deserialzable from json" when itReceives {
      "a get to its base" in new Fixture1 {
        Get("/me") ~>  api ~> check {
          val res = entityAs[JObject]
          println(res)
          res.extract[User] should be (fakeUser1)
        }
      }
    }
    "produce the correct value" when itReceives {
      "a get for the id field" in new Fixture1 {
        Get("/me/id") ~> api ~> check {
          val res = (entityAs[JObject] \ User.ID).extract[String]
          res should be (userId)
        }
      }
      "a get for the nick field" in new Fixture1 {
        Get("/me/nick") ~> api ~> check {
          val res = (entityAs[JObject] \ User.NICK).extract[String]
          assert(res === "this user")
        }
      }
      "a get for the public field" in new Fixture1 {
        Get("/me/public") ~> api ~> check {
          val res = (entityAs[JObject] \ User.PUBLIC).extract[Boolean]
          assert(res === fakeUser1.public)
        }
      }
      "a get for the info field" in new Fixture1 {
        Get("/me/info") ~>  api ~> check {
          val res = (entityAs[JObject] \ User.INFO).asInstanceOf[JObject]
          res should equal (fakeUser1.info)
        }
      }
      "a get for the following field" in new Fixture1 {
        Get("/me/following") ~>  api ~> check {
          val res = (entityAs[JObject] \ User.FOLLOWING).extract[Set[String]]
          res should equal (fakeUser1.following)
        }
      }
      "a get for the followers field" in new Fixture1 {
        Get("/me/followers") ~>  api ~> check {
          val res = (entityAs[JObject] \ User.FOLLOWERS).extract[Set[String]]
          res should equal (fakeUser1.followers)
        }
      }
      "a get for the blocked field " in new Fixture1 {
        Get("/me/blocked") ~>  api ~> check {
          val res = (entityAs[JObject] \ User.BLOCKED).extract[Set[String]]
          res should equal (fakeUser1.blocked)
        }
      }
      "a get for the topics field" in new Fixture1 {
        Get("/me/topics") ~>  api ~> check {
          val res = (entityAs[JObject] \ User.TOPICS).extract[Set[String]]
          res should equal (fakeUser1.topics)
        }
      }
      "a get for the tags field " in new Fixture1 {
        Get("/me/tags") ~>  api ~> check {
          val res = (entityAs[JObject] \ User.TAGS).extract[Set[String]]
          res should equal (fakeUser1.tags)
        }
      }
      "a query: following contains a user that is followed" in new Fixture1 {
        Get("/me/following/otherUser") ~>  api ~> check {
          assertContains(entityAs[JObject])
        }
      }
      "a query: following contains a user that is not followed" in new Fixture1 {
        Get("/me/following/fakeUser") ~>  api ~> check {
          assertNotContains(entityAs[JObject])
        }
      }
      "a query: followers contains a user that is following" in new Fixture1 {
        Get("/me/followers/otherUser") ~>  api ~> check {
          assertContains(entityAs[JObject])
        }
      }
      "a query: followers contains a user that is not following" in new Fixture1 {
        Get("/me/followers/fakeUser") ~>  api ~> check {
          assertNotContains(entityAs[JObject])
        }
      }
      "a query: blocked contains a user that is blocked" in new Fixture1 {
        Get("/me/blocked/some-blocked") ~>  api ~> check {
          assertContains(entityAs[JObject])
        }
      }
      "a query: blocked contains a user that is not blocked" in new Fixture1 {
        Get("/me/blocked/fakeUser") ~>  api ~> check {
          assertNotContains(entityAs[JObject])
        }
      }
      "a query: topics contains a topic that user does participate in" in new Fixture1 {
        Get("/me/topics/tid0") ~>  api ~> check {
          assertContains(entityAs[JObject])
        }
      }
      "a query: topics contains a topic that user does not participate in" in new Fixture1 {
        Get("/me/topics/fakeTopic") ~>  api ~> check {
          assertNotContains(entityAs[JObject])
        }
      }
      "a query: tags contains a tracked tag" in new Fixture1 {
        Get("/me/tags/tag0") ~>  api ~> check {
          assertContains(entityAs[JObject])
        }
      }
      "a query: tags contains an untracked tagged" in new Fixture1 {
        Get("/me/tags/fakeTag") ~>  api ~> check {
          assertNotContains(entityAs[JObject])
        }
      }
    }
    /*
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
      "a put to /me/info" in new Fixture2(UpdateInfo(("place"  "testing") ->: jEmptyObject)) {
        Put("/me/info", spec.asInstanceOf[UpdateInfo].info.nospaces) ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/following/:id" in new Fixture2(FollowUser("user2")) {
        Put("/me/following/user2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/following/:id with a payload" in new Fixture2(FollowUser("user2", Some(("place" := "testing") ->: jEmptyObject))) {
        Put("/me/following/user2", spec.asInstanceOf[FollowUser].additional.get.nospaces) ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a delete to /me/following/:id" in new Fixture2(UnfollowUser("user2")) {
        Delete("/me/following/user2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a delete to /me/followers/:id" in new Fixture2(RemoveFollower("user2")) {
        Delete("/me/followers/user2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/blocked/:id" in new Fixture2(BlockUser("someuser2")) {
        Put("/me/blocked/someuser2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a delete to /me/blocked/:id" in new Fixture2(UnblockUser("someuser2")) {
        Delete("/me/blocked/someuser2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/topics/:id" in new Fixture2(JoinTopic("topic2")) {
        Put("/me/topics/topic2") ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a put to /me/topics/:id with a payload" in new Fixture2(JoinTopic("topic2", Some(("place" := "testing") ->: jEmptyObject))) {
        Put("/me/topics/topic2", spec.asInstanceOf[JoinTopic].additional.get.nospaces) ~> api ~> check {
          entityAs[Boolean] should be (true)
        }
      }
      "a delete to /me/topics/:id" in new Fixture2(LeaveTopic("topic2")) {
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
    }*/
  }
}