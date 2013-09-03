package chatless.chatservtests

import org.scalatest.WordSpec
import spray.testkit.ScalatestRouteTest
import spray.http.StatusCodes
import chatless._
import org.scalatest.matchers.ShouldMatchers
import org.scalamock.scalatest.MockFactory
import chatless.model.Topic
import chatless.db.TopicDAO
import spray.routing.{HttpService, Directives}
import chatless.services.clientApi.TopicApi
import org.json4s._
import org.json4s.native.JsonMethods._
import spray.httpx.Json4sSupport

class TopicRoutesSpec
  extends WordSpec
  with ServiceSpecBase
  with ScalatestRouteTest
  with ShouldMatchers
  with MockFactory {
  import Topic.{publicFields, participantFields}


  def mkPath(tid: TopicId, field: String) = s"/${chatless.services.TOPIC_API_BASE}/$tid/$field"

  trait Fixture { self =>
    val topicDao: TopicDAO
    val tid: TopicId
    def mkGet(field: String = "") = Get(mkPath(tid, field))
    //rest are lazy vals so topicDao can be set before these are instantiated
    lazy val topicApi = new TopicApi {
      val topicDao = self.topicDao
      override val actorRefFactory = system
    }
    lazy val api = Directives.dynamic { topicApi.topicApi(userId) }
  }

  class GetterFixture(val topic: Topic, val times: Int = 1) extends Fixture {
    val tid = topic.id
    val topicDao = mock[TopicDAO]
    (topicDao.get(_: TopicId)) expects topic.id repeated times returning Some(topic)
  }

  val topic1 = Topic("topic1", "topic 1", true, JObject(), "topic1op", Set.empty[String], Set("topic1op", userId), Set.empty[String])

  val topic2 = Topic("topic2", "topic 1", false, JObject(), "topic2op", Set.empty[String], Set("topic2op"), Set.empty[String])

  "the topic api" when {
    "handling a get request" should {
      "return an object with only the proper fields" when {
        "the caller is a participant in the topic" in new GetterFixture(topic1) {
          mkGet() ~> api ~> check {
            entityAs[JObject].values.keySet should be (participantFields.toSet)
          }
        }
        "the caller is not a participant and the topic is not public" in new GetterFixture(topic2) {
          mkGet() ~> api ~> check {
            entityAs[JObject].values.keySet should be (publicFields.toSet)
          }
        }
        "the caller is not a participant in the topic but the topic is public" in new GetterFixture(topic2.copy(public = true)) {
          mkGet() ~> api ~> check {
            entityAs[JObject].values.keySet should be (participantFields.toSet)
          }
        }
      }
      "return true" when {
        "the participants set is checkd for a user that is participating" in new GetterFixture(topic1) {
          mkGet(s"${Topic.PARTICIPATING}/$userId/") ~> api ~> check {
            assertContains(entityAs[JObject])
          }
        }
      }
      "forbid access" when {
        "the caller does not participate in the topic and the topic is private" when {
          for (f <- (participantFields diff publicFields)) {
            s"and the caller attempts to access $f" in new GetterFixture(topic2) {
              mkGet(f) ~> HttpService.sealRoute(api) ~> check {
                status === StatusCodes.Forbidden
              }
            }
          }
        }
      }
    }
  }
}
