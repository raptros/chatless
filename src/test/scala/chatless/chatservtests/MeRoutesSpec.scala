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
    UserM(userId, "this user", true, jEmptyObject, Nil, Nil, Nil, Nil, Nil)
  )

  val api = meApi2

  def itReceives = afterWord("it receives")
  def theFieldIs = afterWord("the field is")

  "the me api" when itReceives {
    "a get to its base" should {
      "provide the correct user object, deserialzable from json" in {
        Get("/me") ~> addCreds ~> meApi2 ~> check {
          val res = entityAs[String].decodeOption[UserM]
          res should be (dbac.users(userId).some)
        }
      }
    }
    "a get for a specifc field" should {
      "produce the correct value" in {
        Get("/me/uid") ~> addCreds ~> meApi2 ~> check {
          val res = entityAs[String].decodeOption[List[UserId]]
          res should be (List(dbac.users(userId).uid).some)
        }
      }
    }
  }
}
