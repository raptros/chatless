import chatless._
import chatless.db._
import chatless.db.GetFields
import chatless.db.ResTopic
import chatless.db.ResUser
import chatless.services.ServiceBase
import chatless.{UserId, TopicId}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, FunSuite, Suite, WordSpec}
import scala.Some
import spray.http.{HttpRequest, BasicHttpCredentials}
import spray.routing.Route
import spray.routing.authentication.{UserPass, UserPassAuthenticator, BasicAuth, ContextAuthenticator}
import spray.testkit.ScalatestRouteTest

import argonaut._
import Argonaut._

import scala.concurrent._

trait ServiceSpecBase extends
ShouldMatchers
with ScalatestRouteTest
with ServiceBase
with OperationMatchers  { this:FunSpec =>

  val userId = s"test-${this.getClass}-123"
  val password = "hypermachinery"

  def actorRefFactory = system

  /**fake auth stuff for testing*/
  val upa:UserPassAuthenticator[UserId] = { (oup:Option[UserPass]) =>
    future {
      oup flatMap {
        case UserPass(uid, pass) if uid == userId && pass == password => Some(userId)
        case _ => None
      }
    }
  }

  def getUserAuth:ContextAuthenticator[UserId] = BasicAuth(upa, "")

  /** won't ever actually be called for*/
  def dbSel = system.actorSelection("../chatless-service-db")

  override implicit def executionContext = actorRefFactory.dispatcher

  val classList = List(classOf[ResUser], classOf[ResTopic], classOf[OpSpec], classOf[GetFields])


  val addCreds = addCredentials(BasicHttpCredentials(userId, password))

  val apiInspector:Route

  def describeResultOf(req:HttpRequest, auth:Boolean = true)(inner: Operation => Unit) = describe(s"${req.method.value} to ${req.uri}") {
    req ~> { if (auth) addCreds else { (r:HttpRequest) => r } } ~> apiInspector ~> check {
      inner(entityAs[Json].as[Operation].getOr(throw new Exception("json object not extractable to Operation")))
    }
  }


}
