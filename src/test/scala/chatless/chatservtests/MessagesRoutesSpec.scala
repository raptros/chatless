package chatless.chatservtests

import chatless._
import chatless.services.TOPIC_API_BASE
import chatless.services.MESSAGE_API_BASE
import org.scalatest.{WordSpec, Matchers, OptionValues}
import org.scalamock.scalatest.MockFactory
import chatless.db.MessageDAO
import chatless.services.clientApi.MessagesApi
import akka.actor.ActorRefFactory
import chatless.services._
import chatless.ops.TopicOps
import spray.routing.HttpService
import spray.http.StatusCodes
import chatless.model._
import scalaz.{\/-, -\/}

class MessagesRoutesSpec
  extends WordSpec
  with Matchers
  with HttpService
  with OptionValues
  with ServiceSpecBase
  with MockFactory { self =>

  def pMessages(tid: TopicId) = s"/$TOPIC_API_BASE/$tid/$MESSAGE_API_BASE"

  def calledWith = afterWord("called with")

  def sendingTo(topic: Topic) = afterWord(s"sending to ${topic.id} (${topic.title})")

  def newApi(ops: TopicOps, dao: MessageDAO) = {
    val messagesApi = new MessagesApi {
      implicit def actorRefFactory: ActorRefFactory = self.system
      val messageDao = dao
      val topicOps = ops
    }
    dynamic { messagesApi.messagesApi(userId) }
  }

  val topic0 = Topic(
    id = "topic0",
    title = s"user $userId participates, topic isn't muted",
    public = true,
    muted = false,
    info = JDoc(),
    op = "...",
    sops = Set(),
    voiced = Set(),
    users = Set(userId),
    banned = Set(),
    tags = Set())



  trait Fixture { fixture =>
    val messagesDao = mock[MessageDAO]
    val topicOps = mock[TopicOps]

    lazy val api = newApi(topicOps, messagesDao)
  }

  "the messages api" should {
    "return query results" when calledWith {
      "a GET to " in (pending)
    }
    s"appropriately handle messages POSTed by $userId" when sendingTo(topic0) {
      "and the body is valid" in new Fixture {
        val messageId = "fake"
        topicOps.getOrThrow _ expects topic0.id once() returning topic0
        topicOps.sendMessage _ expects (userId, topic0.id, JDoc()) once() returning \/-(messageId)
        Post(pMessages(topic0.id), jsonEntity("{}")) ~> api ~> check {
          status shouldBe StatusCodes.NoContent
          header(X_CREATED_MESSAGE).value should have ('value (messageId))
        }
      }
      "and something goes wrong" in new Fixture {
        val messageId = "fake"
        topicOps.getOrThrow _ expects topic0.id once() returning topic0
        topicOps.sendMessage _ expects (userId, topic0.id, JDoc()) once() returning -\/("mistake!")
        Post(pMessages(topic0.id), jsonEntity("{}")) ~> api ~> check {
          status shouldBe StatusCodes.InternalServerError
          header(X_CREATED_MESSAGE) shouldBe empty
        }
      }
      "and the body is invalid" in new Fixture {
        val messageId = "fake"
        topicOps.getOrThrow _ expects topic0.id once() returning topic0
        topicOps.sendMessage _ expects (*, *, *) never()
        Post(pMessages(topic0.id), jsonEntity("{ \"fnarg\" : ,}")) ~> sealRoute(api) ~> check {
          status shouldBe StatusCodes.BadRequest
        }
      }
    }
  }
}
