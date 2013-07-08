import chatless.db._
import chatless.db.GetFields
import chatless.db.ResTopic
import chatless.db.ResUser
import chatless.{ServiceBase, UserId, TopicId}
import org.json4s.native.Serialization
import org.json4s.{NoTypeHints, FullTypeHints, ShortTypeHints}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, FunSuite, Suite, WordSpec}
import scala.Some
import spray.http.{HttpRequest, BasicHttpCredentials}
import spray.httpx.Json4sSupport
import spray.routing.Route
import spray.routing.authentication.{UserPass, UserPassAuthenticator, BasicAuth, ContextAuthenticator}
import spray.testkit.ScalatestRouteTest

import scala.concurrent._

trait ServiceSpecBase extends Json4sSupport
with ShouldMatchers
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

  implicit val json4sFormats = Serialization.formats(NoTypeHints) //Serialization.formats(FullTypeHints(classList))

  val addCreds = addCredentials(BasicHttpCredentials(userId, password))

  val apiInspector:Route

  def describeResultOf(req:HttpRequest, auth:Boolean = true)(inner: Operation => Unit) = describe(s"${req.method.value} to ${req.uri}") {
    req ~> { if (auth) addCreds else { (r:HttpRequest) => r } } ~> apiInspector ~> check {
      inner(entityAs[Operation])
    }
  }


}
