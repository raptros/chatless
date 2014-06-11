package chatless.ops

import org.scalatest.{FlatSpec, Matchers}
import chatless.MockFactory2
import chatless.db._
import chatless.model.{UserCoordinate, User, ServerCoordinate}
import chatless.model.ids._
import chatless.model.topic.{Member, MemberMode, Topic, TopicInit}
import scalaz.syntax.id._
import scalaz.{-\/, \/-}
import OperationTypes._
import Preconditions._
import chatless.ops.topic.{TopicOps, TopicOpsImpl}

class TopicOpsSpec extends FlatSpec with Matchers with MockFactory2 {

  trait Fixture {
    val serverId = ServerCoordinate("topic-ops-spec".serverId)
    val userDao = mock[UserDAO]
    val topicDao = mock[TopicDAO]
    val topicMemberDao = mock[TopicMemberDAO]
    val messageDao = mock[MessageDAO]

    val topicOps = new TopicOpsImpl(serverId, userDao, topicDao, messageDao, topicMemberDao)
  }

  behavior of "the topic ops create method"

  it should "not allow a topic to be created for a non-local user" in new Fixture {
    val uc = UserCoordinate("bad-server".serverId, "u-fake".userId)
    val user = User(uc.server, uc.id, "fake".topicId, "fake2".topicId, Nil)
    val ti = TopicInit(banner = "create and add")
    val res = topicOps.createTopic(user, ti)
    res shouldBe -\/(PreconditionFailed(CREATE_TOPIC, USER_NOT_LOCAL, "user" -> uc, "server" -> serverId))
  }

  it should "create a topic and add a member" in new Fixture {
    val uc = serverId.user("u1".userId)
    val user = User(uc.server, uc.id, "fake".topicId, "fake2".topicId, Nil)
    val ti = TopicInit(banner = "create and add")
    val tc = uc.topic("t1".topicId)
    val topic = Topic(tc, ti.banner, ti.info, ti.mode)
    topicDao.createLocal _ expects ("u1".userId, ti) returning topic.right
    topicMemberDao.set _ expects (tc, uc, MemberMode.creator) returning Member(tc, uc, MemberMode.creator).right
    val res = topicOps.createTopic(user, ti)
    res shouldBe \/-(Created(topic))
  }

  it should "report a db error preventing it from creating a topic" in new Fixture {
    val uc = serverId.user("u1".userId)
    val user = User(uc.server, uc.id, "fake".topicId, "fake2".topicId, Nil)
    val ti = TopicInit(banner = "create and add")
    val tc = uc.topic("t1".topicId)
    val failure = GenerateIdFailed("topic", uc, "one" :: "two" :: "three" :: Nil)
    topicDao.createLocal _ expects ("u1".userId, ti) returning failure.left
    val res = topicOps.createTopic(user, ti)
    res shouldBe -\/(DbOperationFailed(CREATE_TOPIC, uc, failure))
  }

  behavior of "the topic ops invite method"

  it should "check permissions, add a member, and send an invite message" in pending

  it should "attempt to join the user to the invites topic before adding a member" in pending

  it should "not add a member if it would not be able to send an invite message" in pending

  it should "tell the topic about the invited user" in pending

  behavior of "the topic ops join method"

  it should "set the right mode" in pending

  it should "send the right message to the joined topic" in pending

  it should "not modify an existing membership" in pending
}
