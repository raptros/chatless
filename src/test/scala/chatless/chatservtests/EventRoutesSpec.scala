package chatless.chatservtests

import chatless._
import chatless.model.ContainableValue._
import chatless.services.EVENT_API_BASE
import org.scalatest.WordSpec
import org.scalatest.Matchers._
import org.scalamock.scalatest.MockFactory
import chatless.db.EventDAO
import chatless.services.clientApi.EventApi
import akka.actor.ActorRefFactory
import chatless.ops.UserOps
import spray.routing.Directives
import org.joda.time.DateTime
import spray.http.StatusCodes
import chatless.model._
import scala.Some

class EventRoutesSpec
  extends WordSpec
  with ServiceSpecBase
  with MockFactory { self =>

  def newApi(ops: UserOps, dao: EventDAO) = {
    val eventApi = new EventApi {
      implicit def actorRefFactory: ActorRefFactory = self.system
      val eventDao = dao
      val userOps = ops
    }
    Directives.dynamic { eventApi.eventApi(userId) }
  }

  val user = User(id = userId, "a private user followed",
        true,
        JDoc(),
        Set("one"),
        Set("two"),
        Set.empty[UserId],
        Set(),
        Set())

  trait Fixture { fixture =>
    val eventDao = mock[EventDAO]
    val userOps = mock[UserOps]

    lazy val api = newApi(userOps, eventDao)
  }

  def calledWith = afterWord("called with")

  "the event api" when calledWith {
    "the oldest event is requested" should {
      "complete the request with a single object call" in new Fixture {
        val target = DateTime.now().minusDays(7)
        userOps.getOrThrow _ expects userId once() returning user
        eventDao.oldestKnownEventTime _ expects() once() returning Some(target)
        Get(s"/$EVENT_API_BASE/oldest") ~> api ~> check {
          assert(status === StatusCodes.OK)
          val res = DateTime.parse((parseJObject \ "timestamp").extract[String])
          assert(target.isEqual(res), s"target: $target, res: $res")
        }
      }
      "still complete even with a slash" in new Fixture {
        val target = DateTime.now().minusDays(7)
        userOps.getOrThrow _ expects userId once() returning user
        eventDao.oldestKnownEventTime _ expects() once() returning Some(target)
        Get(s"/$EVENT_API_BASE/oldest/") ~> api ~> check {
          assert(status === StatusCodes.OK)
          val res = DateTime.parse((parseJObject \ "timestamp").extract[String])
          assert(target.isEqual(res), s"target: $target, res: $res")
        }
      }
    }
    "a request with no extra query" should {
      "return the latest event" in new Fixture {
        val expected = Iterable(Event.userUpdate(Action.REPLACE, "one", User.NICK, "pants"))
        userOps.getOrThrow _ expects userId once() returning user
        eventDao.last _ expects (*, 1) once() returning expected
        Get(s"/$EVENT_API_BASE") ~> api ~> check {
          assert(status == StatusCodes.OK)
          val actual = parseJVal.extract[List[Event]]
          actual should have size 1
          actual(0) should have (
            'kind (EventKind.USER_UPDATE),
            'action (Action.REPLACE),
            'field (Some(User.NICK)),
            'value (StringVC("pants"))
          )
        }
      }
    }
    "a request for two items 'from' a particular id" should {
      "call for both and return them" in new Fixture {
        val targetEventId = "324532"
        val expected = Iterable(
          Event.userUpdate(Action.REPLACE, "one", User.NICK, "pants").copy(id = Some(targetEventId)),
          Event.topicUpdate(Action.ADD, "one", "t-1", Topic.BANNED, "three").copy(id = Some("y544t4"))
        )
        userOps.getOrThrow _ expects userId once() returning user
        eventDao.from _ expects (*, targetEventId, 2) once() returning expected
        Get(s"/$EVENT_API_BASE/from/$targetEventId/2/") ~> api ~> check {
          assert(status == StatusCodes.OK)
          val actual = parseJVal.extract[List[Event]]
          actual should have size 2
          actual(0) should have ('kind (EventKind.USER_UPDATE))
          actual(1) should have ('kind (EventKind.TOPIC_UPDATE))
        }
      }
    }
  }
}
