package chatless

import org.scalatest.Suite
import spray.http._
import spray.routing.authentication.{UserPassAuthenticator, BasicAuth, ContextAuthenticator}
import spray.testkit.ScalatestRouteTest


import scala.concurrent._
import spray.routing.authentication.UserPass

import spray.httpx.Json4sSupport
import spray.http.MediaTypes._

trait ServiceSpecBase extends ScalatestRouteTest { this: Suite =>

  def jsonEntity(d: String) = HttpEntity(`application/json`, d)

  def actorRefFactory = system

  val userId = s"test-${this.getClass.getSimpleName}-123"
  val password = "hypermachinery"

  /**fake auth stuff for testing*/
  val upa: UserPassAuthenticator[UserId] = { (oup: Option[UserPass]) =>
    future {
      for {
        UserPass(uid, pass) <- oup
        if uid == userId && pass == password
      } yield uid
    }
  }

  def getUserAuth: ContextAuthenticator[UserId] = BasicAuth(upa, "")

  def addCreds = addCredentials(BasicHttpCredentials(userId, password))


}
