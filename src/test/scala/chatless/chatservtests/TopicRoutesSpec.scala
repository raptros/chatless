package chatless.chatservtests

import org.scalatest.WordSpec
import spray.testkit.ScalatestRouteTest
import spray.http.StatusCodes
import chatless._
import org.scalatest.matchers.ShouldMatchers
import org.scalamock.scalatest.MockFactory
import chatless.model.{JDoc, Topic}
import chatless.db.TopicDAO
import spray.routing.{HttpService, Directives}
import chatless.services.clientApi.TopicApi
import org.json4s._
import org.json4s.native.JsonMethods._
import spray.httpx.Json4sSupport
import scalaz._
import spray.httpx.marshalling.Marshaller
import akka.event.Logging

class TopicRoutesSpec
  extends WordSpec
  with ServiceSpecBase
  with ScalatestRouteTest
  with ShouldMatchers
  with HttpService
  with MockFactory {
  import Topic.{publicFields, participantFields}


  def mkPath(tid: TopicId, field: String) = s"/${chatless.services.TOPIC_API_BASE}/$tid/$field"

  trait Fixture { self =>
    val topicDao: TopicDAO
    val tid: TopicId

    def mkGet(field: String = "") = Get(mkPath(tid, field))

    def mkPut[A : Marshaller](field: String, value: A) = Put(mkPath(tid, field), value)

    def mkPut(field: String) = Put(mkPath(tid, field))

    def mkDelete(field: String) = Delete(mkPath(tid, field))

    //rest are lazy vals so topicDao can be set before these are instantiated
    lazy val topicApi = new TopicApi {
      val log = Logging(system, "topicApi in TopicRouesSpec")
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

  class Fixture2(op: String, sops: Set[String], participants: Set[String]) extends Fixture {
    val tid = "testTopic"
    val topicDao = mock[TopicDAO]
    val topic = Topic(tid, "topic 1", true, JDoc(), op, sops, participants, Set.empty[String])
  }

  val topic1 = Topic("topic1", "topic 1", true, JDoc(), "topic1op", Set.empty[String], Set("topic1op", userId), Set.empty[String])

  val topic2 = Topic("topic2", "topic 1", false, JDoc(), "topic2op", Set.empty[String], Set("topic2op"), Set.empty[String])

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
        s"the caller requests /topic/:tid/${Topic.TITLE}" in new GetterFixture(topic1) {
          mkGet(Topic.TITLE) ~> api ~> check {
            val res = (entityAs[JObject] \ Topic.TITLE).extract[String]
            res === topic.title
          }
        }
        s"the caller requests /topic/:tid/${Topic.PUBLIC}" in new GetterFixture(topic1) {
          mkGet(Topic.PUBLIC) ~> api ~> check {
            val res = (entityAs[JObject] \ Topic.PUBLIC).extract[Boolean]
            res === topic.public
          }
        }
        s"the caller requests /topic/:tid/${Topic.INFO}" in new GetterFixture(topic1) {
          mkGet(Topic.INFO) ~> api ~> check {
            val res = (entityAs[JObject] \ Topic.INFO).asInstanceOf[JObject]
            res === topic.info
          }
        }
      }
      "return ok" when {
        "the participants set is checkd for a user that is participating" in new GetterFixture(topic1) {
          mkGet(s"${Topic.PARTICIPATING}/$userId/") ~> api ~> check {
            status === StatusCodes.NoContent
          }
        }
      }
      "return not found" when {
        "the participants set is checked for a non-participating user" in new GetterFixture(topic1) {
          mkGet(s"${Topic.PARTICIPATING}/fakeUser/") ~> api ~> check {
            status === StatusCodes.NotFound
          }
        }
      }
      "forbid access" when {
        "the caller does not participate in the topic and the topic is private" when {
          for (f <- participantFields diff publicFields) {
            s"and the caller attempts to access $f" in new GetterFixture(topic2) {
              mkGet(f) ~> HttpService.sealRoute(api) ~> check {
                status === StatusCodes.Forbidden
              }
            }
          }
        }
      }
    }
    s"""handling a put to ${mkPath("testTopic", "title")}""" should {
      "forbid access" when {
        "the user is not at least a second op" in new Fixture2("notuser", Set.empty[String], Set(userId)) {
          (topicDao.get(_: TopicId)) expects topic.id once() returning Some(topic)
          (topicDao.setTitle(_: TopicId, _: String)) expects (*, *) never()
          mkPut(Topic.TITLE, "newTitle") ~> sealRoute(api) ~> check {
            status === StatusCodes.Forbidden
          }
        }
      }
      "update the dao properly" in new Fixture2("notuser", Set(userId), Set(userId)) {
        (topicDao.get(_: TopicId)) expects topic.id once() returning Some(topic)
        (topicDao.setTitle(_: TopicId, _: String)) expects (*, *) once() returning \/-(true)
        mkPut(Topic.TITLE, "newTitle") ~> api ~> check {
          status === StatusCodes.NoContent
          header("x-chatless-updated").nonEmpty
        }
      }
    }
  }
}
