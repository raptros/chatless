package chatless

import org.scalatest.Suite
import spray.http._
import spray.routing.authentication.{UserPassAuthenticator, BasicAuth, ContextAuthenticator}
import spray.testkit.ScalatestRouteTest


import scala.concurrent._
import spray.routing.authentication.UserPass

trait ServiceSpecBase extends ScalatestRouteTest { this: Suite =>

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

}
