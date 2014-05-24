package chatless
import org.scalatest.{FlatSpec, WordSpec, Matchers}
import spray.testkit.ScalatestRouteTest
import spray.routing._
import chatless._
import scalaz._
import scalaz.syntax.id._
import chatless.services.MarshallingImplicits._

import chatless.db.{TopicDAO, UserDAO}
import chatless.model._
import spray.http.MediaTypes._
import chatless.services.ClientApi
import akka.actor.ActorRefFactory
import chatless.model.topic.{TopicMode, Topic, TopicInit}
import argonaut._
import Argonaut._
import spray.http.HttpHeaders.Location
import spray.http.StatusCodes

class ApiTests extends FlatSpec
  with ScalatestRouteTest
  with Matchers
  with MockFactory2 {

  trait Fixture { self =>
    val uDao = mock[UserDAO]
    val tDao = mock[TopicDAO]
    val sc = ServerCoordinate("test")

    val api = new ClientApi {
      val serverId = sc
      val userDao = self.uDao
      val topicDao = self.tDao

      override implicit def actorRefFactory: ActorRefFactory = system
    }
  }

  val user1 = User("test", "one", "about", Nil)

  behavior of "the client api"

  it should "handle a get to /me/topic by returning json" in new Fixture {
    uDao.get _ expects user1.id once() returning user1.right
    tDao.listUserTopics _ expects * once() returning List(TopicCoordinate("test", "one", "fake"))
    Get("/me/topic/") ~> api.authedApi(user1.id) ~> check {
      mediaType === `application/json`
      entity should not be empty
    }
  }

  it should "create a topic for a post to /me/topic/" in new Fixture {
    uDao.get _ expects user1.id once() returning user1.right
    val ti = TopicInit(banner = "test1")
    val topic = Topic(user1.coordinate.topic("test1-id"), banner = "test1", jEmptyObject, TopicMode.default)
    tDao.createLocal _ expects (user1.id, ti) once() returning topic.right
    Post("/me/topic", ti) ~> api.authedApi(user1.id) ~> check {
      mediaType === `application/json`
      status shouldBe StatusCodes.Created
      header[Location] should not be empty
      header[Location].get.uri.toString should include (topic.id)
      entity should not be empty
      responseAs[Topic] shouldBe topic
    }
  }
}
