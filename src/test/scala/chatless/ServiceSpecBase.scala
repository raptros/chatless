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
import chatless.operation.GetFields

trait ServiceSpecBase extends
ShouldMatchers
with ScalatestRouteTest
with ServiceBase
with OperationMatchers  { this:FunSpec =>

  val userId = s"test-${this.getClass.getSimpleName}-123"
  val password = "hypermachinery"

  def actorRefFactory = system

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

  /** won't ever actually be called for*/
  def dbSel = system.actorSelection("../chatless-service-db")

  override implicit def executionContext = actorRefFactory.dispatcher

  val classList = List(classOf[ResUser], classOf[ResTopic], classOf[OpSpec], classOf[GetFields])


  val addCreds = addCredentials(BasicHttpCredentials(userId, password))

  val apiInspector:Route

//  def describeRequest(req:HttpRequest, auth:Boolean=true)(to:Route)(inner:RouteResult => Unit)

  def describeResultOf(req:HttpRequest, auth:Boolean = true)(inner: Operation => Unit) = {
    val entityString = req.entity.toOption map { b:HttpBody =>
      s" with entity ${b.asString}"
    } getOrElse ""
    val name = s"${req.method.value} to ${req.uri}" + entityString
    describe(name) {
      val oEnt:Option[HttpEntity] = req ~> { if (auth) addCreds else { (r:HttpRequest) => r } } ~> apiInspector ~> check {
        if (handled) Some(entity) else None
      }
      val oJson = oEnt flatMap { e => e.as[Json].right.toOption }
      val oOperation = oJson flatMap { j => j.as[Operation].value }

      it("must have produced an actual operation") {
        assert(oEnt.nonEmpty, "it wasn't actually handled")
        assert(oJson.nonEmpty, s"couldn't get the json out of the entity $oEnt")
        assert(oOperation.nonEmpty, s"couldn't get the operation out of the json $oJson")
      }
      oOperation foreach inner
    }
  }

  def operationTest(eRes:OpRes, eSpec:OpSpec)(op:Operation) = {
    it("contains the user's id") {
      op should have (cid (userId))
    }
    it(s"selects resource $eRes") {
      op should have (res (eRes))
    }
    it(s"specifies the action $eSpec") {
      op should have (spec (eSpec))
    }
  }


}
