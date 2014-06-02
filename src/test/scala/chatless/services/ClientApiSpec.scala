package chatless.services

import org.scalatest.{FlatSpec, Matchers}
import spray.testkit.ScalatestRouteTest
import chatless._
import scalaz.syntax.id._
import scalaz.syntax.std.either._

import chatless.db.{TopicMemberDAO, NoSuchObject, TopicDAO, UserDAO}
import chatless.model._
import chatless.model.ids._
import spray.http.MediaTypes._
import akka.actor.ActorRefFactory
import chatless.model.topic.{TopicMode, Topic, TopicInit}
import argonaut._
import Argonaut._
import spray.http.HttpHeaders.Location
import spray.http.{HttpResponse, StatusCodes}
import chatless.ops.{Created, TopicOps}
import spray.httpx.unmarshalling._
import spray.routing.{Route, Directives}

class ClientApiSpec extends FlatSpec
  with ScalatestRouteTest
  with Matchers
  with MockFactory2 {

  trait Fixture { self =>
    val uDao = mock[UserDAO]
    val tDao = mock[TopicDAO]
    val tmDao = mock[TopicMemberDAO]
    val topicOps = mock[TopicOps]
    val sc = ServerCoordinate("test".serverId)
    val user1 = User("test".serverId, "one".userId, "about".topicId, Nil)

    def clientApi = new ClientApi {
      val serverId = sc
      val userDao = self.uDao
      val topicDao = self.tDao
      val topicMemberDao = self.tmDao
      val topicOps = self.topicOps

      override implicit def actorRefFactory: ActorRefFactory = system
    }

    val authedApi: Route = Directives.dynamic { clientApi.authedApi(user1.id) }

  }
  import TildeArrow.injectIntoRoute

  behavior of "the client api"

  it should "handle a get to /me/topic by returning json" in new Fixture {
    uDao.get _ expects user1.coordinate once() returning user1.right
    tDao.listUserTopics _ expects * once() returning List(TopicCoordinate("test".serverId, "one".userId, "fake".topicId)).right
    (Get("/me/topic/") ~> authedApi)(injectIntoRoute) ~> check {
      mediaType === `application/json`
      entity should not be empty
      val coords = responseAs[List[TopicCoordinate]]
      coords should not be empty
      coords(0) should have ('id ("fake"))
    }
  }

  it should "create a topic for a post to /me/topic/" in new Fixture {
    uDao.get _ expects user1.coordinate once() returning user1.right
    val ti = TopicInit(banner = "test1")
    val topic = Topic(user1.coordinate.topic("test1-id".topicId), banner = "test1", jEmptyObject, TopicMode.default)
    topicOps.createTopic _ expects (user1, ti) once() returning Created(topic).right
    (Post("/me/topic", ti) ~> authedApi)(injectIntoRoute) ~> check {
      mediaType === `application/json`
      status shouldBe StatusCodes.Created
      header[Location] should not be empty
      header[Location].get.uri.toString should include (topic.id)
      body.as[Topic].disjunction valueOr failLeft("unmarshal failed") shouldBe topic
    }
  }

  it should "get a local topic" in new Fixture {
    uDao.get _ expects user1.coordinate once() returning user1.right
    val topic = Topic(user1.coordinate.topic("test2-id".topicId), banner = "test2", jEmptyObject, TopicMode.default)
    tDao.get _ expects topic.coordinate once() returning topic.right
    (Get("/me/topic/test2-id") ~> authedApi)(injectIntoRoute) ~> check {
      mediaType === `application/json`
      status shouldBe StatusCodes.OK
      entity should not be empty
      body.as[Topic].disjunction valueOr failLeft("body unmarshal failed") shouldBe topic
    }
  }

  it should "return the proper error if the topic is not found" in new Fixture {
    uDao.get _ expects user1.coordinate once() returning user1.right
    val tc = user1.coordinate.topic("test-notfound".topicId)
    tDao.get _ expects tc once() returning NoSuchObject(tc).left
    (Get(s"/me/topic/${tc.id}") ~> authedApi)(injectIntoRoute) ~> check {
      mediaType === `application/json`
      status shouldBe StatusCodes.NotFound
      val js = body.as[Json].disjunction valueOr { e => fail(s"could not read body as json: $e") }
      js -| "description" getOrElse fail("no description!") stringOr fail("description not a string!") shouldBe "MISSING"
    }
  }

  private def failLeft[A](what: String): A => Nothing = a => fail(s"$what: $a")
}
