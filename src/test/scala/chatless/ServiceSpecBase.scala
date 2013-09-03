package chatless

import org.scalatest.Suite
import spray.http._
import spray.routing.authentication.{UserPassAuthenticator, BasicAuth, ContextAuthenticator}
import spray.testkit.ScalatestRouteTest


import scala.concurrent._
import spray.routing.authentication.UserPass

import org.json4s._
import org.json4s.JsonDSL._
import spray.httpx.Json4sSupport
import chatless.model.InfoSerializer

trait ServiceSpecBase extends ScalatestRouteTest with Json4sSupport { this: Suite =>

  implicit val json4sFormats = DefaultFormats + (new InfoSerializer)

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

  /** won't ever actually be called for*/
  def dbSel = system.actorSelection("../chatless-service-db")

  def assertContains(res: JObject) = {
    assert((res \ "contains").extract[Boolean])
  }

  def assertNotContains(res: JObject) = {
    assert(!(res \ "contains").extract[Boolean])
  }


}
