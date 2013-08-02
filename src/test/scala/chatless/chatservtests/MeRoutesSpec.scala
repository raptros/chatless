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

class MeRoutesSpec
  extends WordSpec
  with ScalatestRouteTest
  with HttpService
  with MeApi
  with ServiceSpecBase
  with ShouldMatchers {

  val dbac = FakeDbAccess(
    UserM(userId, "this user", true, jEmptyObject,
      Set("otherUser"), Set("otherUser"), Set("some-blocked"),
      Set("tid0"), Set("tag0"))
  )

  val api = meApi2

  def itReceives = afterWord("it receives")
  def theFieldIs = afterWord("the field is")

  "the me api" should {
    "provide the correct user object, deserialzable from json" when itReceives {
      "a get to its base" in {
        (Get("/me") ~> addCreds) ~> api ~> check {
          val res = entityAs[String].decodeOption[UserM]
          res should be (dbac.users(userId).some)
        }
      }
    }
    "produce the correct value" when itReceives {
      "a get for the uid field" in {
        Get("/me/uid") ~> addCreds ~> api ~> check {
          val res = entityAs[String]
          assert(res === userId)
        }
      }
      "a get for the nick field" in {
        Get("/me/nick") ~> addCreds ~> api ~> check {
          val res = entityAs[String]
          assert(res === "this user")
        }
      }
      "a get for the public field" in {
        Get("/me/public") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          assert(res === true)
        }
      }
      "a get for the info field" in {
        Get("/me/info") ~> addCreds ~> api ~> check {
          val res = entityAs[String].parseOption.get
          res should equal (dbac.users(userId).info)
        }
      }
      "a get for the following field" in {
        Get("/me/following") ~> addCreds ~> api ~> check {
          val res = entityAs[String].decodeOption[Set[UserId]].get
          res should equal (dbac.users(userId).following)
        }
      }
      "a get for the followers field" in {
        Get("/me/followers") ~> addCreds ~> api ~> check {
          val res = entityAs[String].decodeOption[Set[UserId]].get
          res should equal (dbac.users(userId).followers)
        }
      }
      "a get for the blocked field " in {
        Get("/me/blocked") ~> addCreds ~> api ~> check {
          val res = entityAs[String].decodeOption[Set[UserId]].get
          res should equal (dbac.users(userId).blocked)
        }
      }
      "a get for the topics field" in {
        Get("/me/topics") ~> addCreds ~> api ~> check {
          val res = entityAs[String].decodeOption[Set[TopicId]].get
          res should equal (dbac.users(userId).topics)
        }
      }
      "a get for the tags field " in {
        Get("/me/tags") ~> addCreds ~> api ~> check {
          val res = entityAs[String].decodeOption[Set[TopicId]].get
          res should equal (dbac.users(userId).tags)
        }
      }
      "a get for a set-containment query" in {
        Get("/me/following/otherUser") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
        Get("/me/following/fakeUser") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
        Get("/me/followers/otherUser") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
        Get("/me/followers/fakeUser") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
        Get("/me/blocked/some-blocked") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
        Get("/me/blocked/fakeUser") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
        Get("/me/topics/tid0") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
        Get("/me/topics/fakeTopic") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
        Get("/me/tags/tag0") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (true)
        }
        Get("/me/tags/fakeTag") ~> addCreds ~> api ~> check {
          val res = entityAs[Boolean]
          res should be (false)
        }
      }
    }
  }
}