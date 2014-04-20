package chatless
import org.scalatest.WordSpec
import spray.testkit.ScalatestRouteTest
import spray.routing._
import chatless._
import scalaz._

import org.scalamock.scalatest.MockFactory
import chatless.db.{TopicDAO, UserDAO}
import chatless.model.{TopicCoordinate, UserCoordinate, User}
import spray.http.MediaTypes._
import spray.httpx.Json4sSupport
import spray.httpx.marshalling.BasicMarshallers._
import spray.httpx.unmarshalling.BasicUnmarshallers._
import spray.http.{StatusCodes, StatusCode}
import akka.event.{LoggingAdapter, Logging}
import org.scalatest.Matchers
import chatless.services.ClientApi
import akka.actor.ActorRefFactory

class ApiTests extends WordSpec
  with ScalatestRouteTest
  with Matchers
  with MockFactory {

  trait Fixture { self =>
    val uDao = mock[UserDAO]
    val tDao = mock[TopicDAO]

    val api = new ClientApi {
      val userDao = self.uDao
      val topicDao = self.tDao

      override implicit def actorRefFactory: ActorRefFactory = system
    }
  }

  val user1 = User("test", "one", "about", Nil)

  "client api meRoute" when {
    "handling a GET to /me/topic" should {
      "return json" in  new Fixture {
        uDao.get _ expects user1.id once() returning Some(user1)
        tDao.listUserTopics _ expects * once() returning List(TopicCoordinate("test", "one", "fake"))
        Get("/me/topic/") ~> api.authedApi(user1.id) ~> check {
          mediaType === `application/json`
          assert(entity.nonEmpty, "why is the entity empty")
        }
      }
    }
  }
}
