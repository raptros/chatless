package chatless

import chatless.db._
import chatless.services.ServiceBase
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, FunSuite, Suite, WordSpec}
import scala.Some
import spray.http._
import spray.httpx._
import spray.httpx.unmarshalling._
import spray.routing.Route
import spray.routing.authentication.{UserPass, UserPassAuthenticator, BasicAuth, ContextAuthenticator}
import spray.testkit.ScalatestRouteTest

import argonaut._
import Argonaut._

import scala.concurrent._
import chatless.operation._
import spray.http.HttpRequest
import spray.routing.authentication.UserPass
import chatless.operation.ResUser
import scala.Some
import chatless.operation.ResTopic
import chatless.operation.GetField

trait ServiceSpecBase extends ScalatestRouteTest { this:Suite =>

  def actorRefFactory = system

  val userId = s"test-${this.getClass.getSimpleName}-123"
  val password = "hypermachinery"

  /**fake auth stuff for testing*/
  val upa:UserPassAuthenticator[UserId] = { (oup:Option[UserPass]) =>
    future {
      for {
        UserPass(uid, pass) <- oup
        if uid == userId && pass == password
      } yield uid
    }
  }

  def getUserAuth:ContextAuthenticator[UserId] = BasicAuth(upa, "")

  def addCreds = addCredentials(BasicHttpCredentials(userId, password))

  /** won't ever actually be called for*/
  def dbSel = system.actorSelection("../chatless-service-db")

  def api:Route

}
