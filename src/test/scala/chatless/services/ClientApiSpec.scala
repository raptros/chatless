package chatless.services

import org.scalatest.{Inspectors, FlatSpec, Matchers}
import spray.testkit.ScalatestRouteTest
import chatless._
import scalaz.syntax.id._
import scalaz.syntax.std.either._

import chatless.db.{TopicMemberDAO, NoSuchObject, TopicDAO, UserDAO}
import chatless.model._
import chatless.model.ids._
import spray.http.MediaTypes._
import akka.actor.ActorRefFactory
import chatless.model.topic._
import argonaut._
import Argonaut._
import spray.http.HttpHeaders.Location
import spray.http.{HttpResponse, StatusCodes}
import chatless.ops.Created
import spray.httpx.unmarshalling._
import spray.routing.{Route, Directives}
import chatless.ops.topic.TopicOps
import chatless.ops.Created
import chatless.db.NoSuchObject

class ClientApiSpec extends FlatSpec
  with ScalatestRouteTest
  with Inspectors
  with Matchers
  with MockFactory2 {

  trait Fixture { self =>
    val uDao = mock[UserDAO]
    val tDao = mock[TopicDAO]
    val tmDao = mock[TopicMemberDAO]
    val topicOps = mock[TopicOps]
    val sc = ServerCoordinate("test".serverId)
    val user1 = User("test".serverId, "one".userId, "about".topicId, "invite".topicId, Nil)

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
      info(js.nospaces)
      js -| "reason" getOrElse fail("no description!") stringOr fail("description not a string!") shouldBe "MISSING"
    }
  }

  it should "get a single member" in new Fixture {
    uDao.get _ expects user1.coordinate once () returning user1.right
    val tc = user1.coordinate.topic("test-listmembers".topicId)
    val topic = Topic(tc, banner = "test2", jEmptyObject, TopicMode.default)
    tDao.get _ expects tc once() returning topic.right
    val returnedMode = MemberMode.modeDeny.copy(read = true, write = true, setBanner = true)
    val target = sc.user("target1".userId)
    topicOps.getMember _ expects (user1, topic, target) returning Some(returnedMode).right
    (Get(s"/me/topic/${tc.id}/member/user/${target.id}") ~> authedApi)(injectIntoRoute) ~> check {
      mediaType === `application/json`
      status === StatusCodes.OK
      val mode = body.as[MemberMode].disjunction valueOr failLeft("MemberMode decode error")
      mode shouldBe returnedMode
    }
  }

  it should "report not found for a non-member" in new Fixture {
    uDao.get _ expects user1.coordinate once () returning user1.right
    val tc = user1.coordinate.topic("test-listmembers".topicId)
    val topic = Topic(tc, banner = "test2", jEmptyObject, TopicMode.default)
    tDao.get _ expects tc once() returning topic.right
    val target = UserCoordinate("fakeServer".serverId, "fakeUser".userId)
    topicOps.getMember _ expects (user1, topic, target) returning None.right
    (Get(s"/me/topic/${tc.id}/member/server/${target.server}/user/${target.user}/") ~> authedApi)(injectIntoRoute) ~> check {
      status === StatusCodes.NotFound
    }
  }

  it should "respond with a list of members" in new Fixture {
    uDao.get _ expects user1.coordinate once () returning user1.right
    val tc = user1.coordinate.topic("test-listmembers".topicId)
    val topic = Topic(tc, banner = "test2", jEmptyObject, TopicMode.default)
    tDao.get _ expects tc once() returning topic.right
    val members = List(
      PartialMember(sc.user("fake1".userId), MemberMode.modeDeny.copy(read = true)),
      PartialMember(sc.user("fake2".userId), MemberMode.modeDeny.copy(read = true, write = true))
    )
    topicOps.getMembers _ expects (user1, topic) once() returning members.right
    (Get(s"/me/topic/${tc.id}/member/") ~> authedApi)(injectIntoRoute) ~> check {
      mediaType === `application/json`
      status === StatusCodes.OK
      entity should not be empty
      val jsonEntity =  body.as[Json].disjunction valueOr failLeft("json parse faiure")
      info(jsonEntity.nospaces)
      val memberList = jsonEntity.as[List[PartialMember]].result valueOr { case (s, ch) => fail(s"decode failed: $s, history: $ch") }
      memberList shouldBe members
    }
  }

  private def failLeft[A](what: String): A => Nothing = a => fail(s"$what: $a")
}
