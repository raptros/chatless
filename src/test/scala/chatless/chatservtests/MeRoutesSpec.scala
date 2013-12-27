package chatless.chatservtests

import org.scalatest.WordSpec

import spray.testkit.ScalatestRouteTest
import spray.routing._
import chatless._
import scalaz._

import org.scalamock.scalatest.MockFactory
import chatless.db.UserDAO
import chatless.model.{JDoc, User}
import chatless.services.clientApi.MeApi
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import spray.httpx.Json4sSupport
import spray.httpx.marshalling.BasicMarshallers._
import spray.httpx.unmarshalling.BasicUnmarshallers._
import chatless.responses.{BoolR, StringR}
import spray.http.{StatusCodes, StatusCode}
import akka.event.{LoggingAdapter, Logging}
import chatless.ops.UserOps
import org.scalatest.Matchers._

class MeRoutesSpec
  extends WordSpec
  with ServiceSpecBase
  with MockFactory { self =>

  val fakeUser1 = User(userId, "this user", true, JDoc("contact?" -> JBool(true)),
    Set("otherUser"), Set("otherUser"), Set("some-blocked"),
    Set("tid0"), Set("tag0"))

  def newApi(ops: UserOps) = {
    val me = new MeApi { api =>
      val userOps = ops
      val actorRefFactory = self.system
    }
    Directives.dynamic { me.meApi(userId) }
  }

  trait BaseFixture {
    val userOps: UserOps
    lazy val api = newApi(userOps)
  }

  trait Fixture1 extends BaseFixture { self =>
    val userOps = mock[UserOps]
    (userOps.getOrThrow(_: UserId)) expects userId returning fakeUser1
  }


  def itReceives = afterWord("it receives")
  def theFieldIs = afterWord("the field is")

  "the me api" should {
    "provide the correct user object, deserialzable from json" when itReceives {
      "a get to its base" in new Fixture1 {
        Get("/me") ~>  api ~> check {
          val res = parseJObject
          res.extract[User] should be (fakeUser1)
        }
      }
    }
    "produce the correct value" when itReceives {
      "a get for the id field" in new Fixture1 {
        Get("/me/id") ~> api ~> check {
          val res = (parseJObject \ User.ID).extract[String]
          res should be (userId)
        }
      }
      "a get for the nick field" in new Fixture1 {
        Get("/me/nick") ~> api ~> check {
          val res = (parseJObject \ User.NICK).extract[String]
          assert(res === "this user")
        }
      }
      "a get for the public field" in new Fixture1 {
        Get("/me/public") ~> api ~> check {
          val res = (parseJObject \ User.PUBLIC).extract[Boolean]
          assert(res === fakeUser1.public)
        }
      }
      "a get for the info field" in new Fixture1 {
        Get("/me/info") ~>  api ~> check {
          val res = (parseJObject \ User.INFO).asInstanceOf[JObject]
          res should equal (fakeUser1.info)
        }
      }
      "a get for the following field" in new Fixture1 {
        Get("/me/following") ~>  api ~> check {
          val res = (parseJObject \ User.FOLLOWING).extract[Set[String]]
          res should equal (fakeUser1.following)
        }
      }
      "a get for the followers field" in new Fixture1 {
        Get("/me/followers") ~>  api ~> check {
          val res = (parseJObject \ User.FOLLOWERS).extract[Set[String]]
          res should equal (fakeUser1.followers)
        }
      }
      "a get for the blocked field " in new Fixture1 {
        Get("/me/blocked") ~>  api ~> check {
          val res = (parseJObject \ User.BLOCKED).extract[Set[String]]
          res should equal (fakeUser1.blocked)
        }
      }
      "a get for the topics field" in new Fixture1 {
        Get("/me/topics") ~>  api ~> check {
          val res = (parseJObject \ User.TOPICS).extract[Set[String]]
          res should equal (fakeUser1.topics)
        }
      }
      "a get for the tags field " in new Fixture1 {
        Get("/me/tags") ~>  api ~> check {
          val res = (parseJObject \ User.TAGS).extract[Set[String]]
          res should equal (fakeUser1.tags)
        }
      }
      "a query: following contains a user that is followed" in new Fixture1 {
        Get("/me/following/otherUser") ~>  api ~> check {
          status shouldBe StatusCodes.NoContent
        }
      }
      "a query: following contains a user that is not followed" in new Fixture1 {
        Get("/me/following/fakeUser") ~>  api ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
      "a query: followers contains a user that is following" in new Fixture1 {
        Get("/me/followers/otherUser") ~>  api ~> check {
          status shouldBe StatusCodes.NoContent
        }
      }
      "a query: followers contains a user that is not following" in new Fixture1 {
        Get("/me/followers/fakeUser") ~>  api ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
      "a query: blocked contains a user that is blocked" in new Fixture1 {
        Get("/me/blocked/some-blocked") ~>  api ~> check {
          status shouldBe StatusCodes.NoContent
        }
      }
      "a query: blocked contains a user that is not blocked" in new Fixture1 {
        Get("/me/blocked/fakeUser") ~>  api ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
      "a query: topics contains a topic that user does participate in" in new Fixture1 {
        Get("/me/topics/tid0") ~>  api ~> check {
          status shouldBe StatusCodes.NoContent
        }
      }
      "a query: topics contains a topic that user does not participate in" in new Fixture1 {
        Get("/me/topics/fakeTopic") ~>  api ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
      "a query: tags contains a tracked tag" in new Fixture1 {
        Get("/me/tags/tag0") ~>  api ~> check {
          status shouldBe StatusCodes.NoContent
        }
      }
      "a query: tags contains an untracked tagged" in new Fixture1 {
        Get("/me/tags/fakeTag") ~>  api ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
    }
  }

  trait Fixture2 extends BaseFixture {
    val userDao = mock[UserDAO]
    val userOps = mock[UserOps]
  }

  "the /me/ api" when itReceives {
    "a PUT to /me/nick" should {
      "perform the appropriate user op" in new Fixture2 {
        (userOps.setNick(_: UserId, _: String)) expects(userId, "heyListen") returning \/-(true)
        Put("/me/nick/", "heyListen") ~> api ~> check {
          status shouldBe StatusCodes.NoContent
          header("x-chatless-updated").nonEmpty
        }
      }
    }
    "a PUT to /me/public" should {
      "update the user correctly for valid args" in new Fixture2 {
        (userOps.setPublic(_: UserId, _: Boolean)) expects(userId, *) returning \/-(true)
        Put("/me/public", true.toString) ~> api ~> check {
          status shouldBe StatusCodes.NoContent
          header("x-chatless-updated").nonEmpty
        }
      }
      "not update the user for badly formed args" in new Fixture2 {
        (userOps.setPublic(_: UserId, _: Boolean)) expects(*, *) never()
        Put("/me/public", "antsg") ~> HttpService.sealRoute(api) ~> check {
          status shouldBe StatusCodes.BadRequest
        }
      }
    }
    "a put to /me/info" should {
      "update the user for correctly formed json" in new Fixture2 {
        val jStr = JDoc("hi" -> JBool(true), "also" -> JArray(JBool(true) :: JInt(34) :: Nil))
        (userOps.setInfo(_: UserId, _: JDoc)) expects (userId, jStr) returning \/-(true) once()
        Put("/me/info", jsonEntity(compact(render(jStr)))) ~> api ~> check {
          status shouldBe StatusCodes.NoContent
          header("x-chatless-updated").nonEmpty
        }
      }
      "reject any badly formed json" in new Fixture2 {
        (userOps.setInfo(_: UserId, _: JDoc)) expects (*, *) never()
        Put("/me/info", jsonEntity("""{"hi": "bye", """)) ~> HttpService.sealRoute(api) ~> check {
          status shouldBe StatusCodes.BadRequest
        }
      }
      "reject when the body is empty" in new Fixture2 {
        (userOps.setInfo(_: UserId, _: JDoc)) expects (*, *) never()
        Put("/me/info", jsonEntity("""{"hi": "bye", """)) ~> HttpService.sealRoute(api) ~> check {
          status shouldBe StatusCodes.BadRequest
        }
      }
    }
  }
}