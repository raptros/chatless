package chatless.chatservtests

import org.scalatest.WordSpec
import spray.http.StatusCodes
import chatless._
import org.scalatest.Matchers._
import org.scalatest.OptionValues._
import org.scalamock.scalatest.MockFactory
import chatless.model.{JDoc, Topic}
import chatless.db.TopicDAO
import spray.routing.{HttpService, Directives}
import chatless.services._
import chatless.services.clientApi.TopicApi
import org.json4s._
import org.json4s.native.Serialization._
import org.json4s.native.JsonMethods._
import scalaz._
import spray.httpx.marshalling.Marshaller
import akka.event.Logging
import chatless.ops.TopicOps
import chatless.model.inits.TopicInit
import akka.actor.ActorRefFactory

class TopicRoutesSpec
  extends WordSpec
  with ServiceSpecBase
  with HttpService
  with MockFactory { self =>
  import Topic.{publicFields, userFields}


  def mkPath(tid: TopicId, field: String) = s"/$TOPIC_API_BASE/$tid/$field"

  def newApi(ops: TopicOps) = {
    val topicApi = new TopicApi {
      implicit def actorRefFactory: ActorRefFactory = self.system
      val topicOps: TopicOps = ops
    }
    Directives.dynamic { topicApi.topicApi(userId) }
  }

  trait Fixture { fixture =>
    val topicOps: TopicOps
    val tid: TopicId

    def mkGet(field: String = "") = Get(mkPath(tid, field))

    def mkPut[A : Marshaller](field: String, value: A) = Put(mkPath(tid, field), value)

    def mkPut(field: String) = Put(mkPath(tid, field))

    def mkDelete(field: String) = Delete(mkPath(tid, field))

    lazy val api = newApi(topicOps)
  }

  class GetterFixture(val topic: Topic, val times: Int = 1) extends Fixture {
    val tid = topic.id
    val topicOps = mock[TopicOps]
    (topicOps.getOrThrow(_: TopicId)) expects topic.id repeated times returning topic
  }

  class PostingFixture extends Fixture {
    val tid = "wh"
    val topicOps = mock[TopicOps]

  }

  class Fixture2(op: String, sops: Set[String], participants: Set[String]) extends Fixture {
    val tid = "testTopic"
    val topicDao = mock[TopicDAO]
    val topicOps = mock[TopicOps]
    val topic = Topic(
      id = tid,
      title = "topic 1",
      public = true,
      muted = false,
      info = JDoc(),
      op = op,
      sops = sops,
      voiced = Set.empty[String],
      users = participants,
      banned = Set.empty[String],
      tags = Set.empty[String])
  }

  val topic1 = Topic(
    id = "topic1",
    title = "topic 1",
    public = true,
    muted = false,
    info = JDoc(),
    op = "topic1op",
    sops = Set.empty[String],
    voiced = Set.empty[String],
    users = Set("topic1op", userId),
    banned = Set.empty[String],
    tags = Set.empty[String])

  val topic2 = Topic(
    id = "topic2",
    title = "topic 1",
    public = false,
    muted = false,
    info = JDoc(),
    op = "topic2op",
    sops = Set.empty[String],
    voiced = Set.empty[String],
    users = Set("topic2op"),
    banned = Set.empty[String],
    tags = Set.empty[String])


  "the topic api" when {
    "handling a get request" should {
      "return an object with only the proper fields" when {
        "the caller is a participant in the topic" in new GetterFixture(topic1) {
          mkGet() ~> api ~> check {
            parseJObject.values.keySet should be (userFields.toSet)
          }
        }
        "the caller is not a participant and the topic is not public" in new GetterFixture(topic2) {
          mkGet() ~> api ~> check {
            parseJObject.values.keySet should be (publicFields.toSet)
          }
        }
        "the caller is not a participant in the topic but the topic is public" in new GetterFixture(topic2.copy(public = true)) {
          mkGet() ~> api ~> check {
            parseJObject.values.keySet should be (userFields.toSet)
          }
        }
        s"the caller requests /topic/:tid/${Topic.TITLE}" in new GetterFixture(topic1) {
          mkGet(Topic.TITLE) ~> api ~> check {
            val res = (parseJObject \ Topic.TITLE).extract[String]
            res === topic.title
          }
        }
        s"the caller requests /topic/:tid/${Topic.PUBLIC}" in new GetterFixture(topic1) {
          mkGet(Topic.PUBLIC) ~> api ~> check {
            val res = (parseJObject \ Topic.PUBLIC).extract[Boolean]
            res === topic.public
          }
        }
        s"the caller requests /topic/:tid/${Topic.INFO}" in new GetterFixture(topic1) {
          mkGet(Topic.INFO) ~> api ~> check {
            val res = (parseJObject \ Topic.INFO).asInstanceOf[JObject]
            res === topic.info
          }
        }
      }
      "return ok" when {
        "the participants set is checkd for a user that is participating" in new GetterFixture(topic1) {
          mkGet(s"${Topic.USERS}/$userId/") ~> api ~> check {
            status shouldBe StatusCodes.NoContent
          }
        }
      }
      "return not found" when {
        "the participants set is checked for a non-participating user" in new GetterFixture(topic1) {
          mkGet(s"${Topic.USERS}/fakeUser/") ~> api ~> check {
            status shouldBe StatusCodes.NotFound
          }
        }
      }
      "forbid access" when {
        "the caller does not participate in the topic and the topic is private" when {
          for (f <- userFields diff publicFields) {
            s"and the caller attempts to access $f" in new GetterFixture(topic2) {
              mkGet(f) ~> HttpService.sealRoute(api) ~> check {
                status shouldBe StatusCodes.Forbidden
              }
            }
          }
        }
      }
    }
    s"""handling a put to ${mkPath("testTopic", "title")}""" should {
      "forbid access" when {
        "the user is not at least a second op" in new Fixture2("notuser", Set.empty[String], Set(userId)) {
          (topicOps.getOrThrow(_: TopicId)) expects topic.id once() returning topic
          (topicOps.setTitle(_: UserId, _: TopicId, _: String)) expects (*, *, *) never()
          mkPut(Topic.TITLE, "newTitle") ~> logRequestResponse("testing sop") { sealRoute(api) } ~> check {
            status shouldBe StatusCodes.Forbidden
          }
        }
      }
      "update the topic properly" in new Fixture2("notuser", Set(userId), Set(userId)) {
        (topicOps.getOrThrow(_: TopicId)) expects topic.id once() returning topic
        (topicOps.setTitle(_: UserId, _: TopicId, _: String)) expects (*, *, *) once() returning \/-(true)
        mkPut(Topic.TITLE, "newTitle") ~> api ~> check {
          status shouldBe StatusCodes.NoContent
          header(X_UPDATED) should not be empty
        }
      }
    }
    "handling a post" should {
      "do a topic create" when {
        "given a simple valid topic init" in new PostingFixture {
          (topicOps.createTopic(_: UserId, _: TopicInit)) expects (*, TopicInit(title = "t1")) returning \/-("fake-id")
          Post(s"/$TOPIC_API_BASE", jsonEntity(write(TopicInit(title = "t1")))) ~> api ~> check {
            status shouldBe StatusCodes.NoContent
            header(X_CREATED_TOPIC).value should have ('value ("fake-id"))
         }
        }
        "given a more complex topic init" in new PostingFixture {
          (topicOps.createTopic(_: UserId, _: TopicInit)) expects
            (*, TopicInit(title = "t1", muted = true, public = false, invite = Set("one", "two"))) returning
            \/-("fake-id")
          val example = """{"title":"t1","muted":true,"public":false,"invite":["one","two"]}"""
          Post(s"/$TOPIC_API_BASE/", jsonEntity(example)) ~> api ~> check {
            status shouldBe StatusCodes.NoContent
            header(X_CREATED_TOPIC).value should have ('value ("fake-id"))
          }
        }
      }
      "fail properly" when {
        "given badly formed json" in new PostingFixture {
          (topicOps.createTopic(_: UserId, _: TopicInit)) expects (*,*) never()
          val example = """{"title": "this", """
          Post(s"/$TOPIC_API_BASE/", jsonEntity(example)) ~> sealRoute(api) ~> check {
            status shouldBe StatusCodes.BadRequest
            header(X_CREATED_TOPIC) shouldBe empty
          }
        }
        "given a topic init object with no title" in new PostingFixture {
          (topicOps.createTopic(_: UserId, _: TopicInit)) expects (*,*) never()
          val example = """{"public": false, "invite": ["one", "two"], "muted": true}"""
          Post(s"/$TOPIC_API_BASE/", jsonEntity(example)) ~> sealRoute(api) ~> check {
            status shouldBe StatusCodes.BadRequest
          }
        }
      }
    }
  }
}
