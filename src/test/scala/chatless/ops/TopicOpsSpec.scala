package chatless.ops

import org.scalatest.{FlatSpec, Matchers}
import chatless.MockFactory2
import chatless.db._
import chatless.model.{UserCoordinate, User, ServerCoordinate}
import chatless.model.ids._
import chatless.model.topic.{Member, MemberMode, Topic, TopicInit}
import scalaz.syntax.id._
import scalaz.{-\/, \/-}

class TopicOpsSpec extends FlatSpec with Matchers with MockFactory2 {

  trait Fixture {
    val serverId = ServerCoordinate("topic-ops-spec".serverId)
    val userDao = mock[UserDAO]
    val topicDao = mock[TopicDAO]
    val topicMemberDao = mock[TopicMemberDAO]
    val messageDao = mock[MessageDAO]

    val topicOps = new TopicOpsImpl(serverId, userDao, topicDao, messageDao, topicMemberDao)
  }

  behavior of "the topic ops tool"

  it should "not allow a topic to be created for a non-local user" in new Fixture {
    val uc = UserCoordinate("bad-server".serverId, "u-fake".userId)
    val user = User(uc.server, uc.id, "fake".topicId, Nil)
    val ti = TopicInit(banner = "create and add")
    val res = topicOps.createTopic(user, ti)
    res shouldBe -\/(UserNotLocal(uc, serverId))
  }

  it should "create a topic and add a member" in new Fixture {
    val uc = serverId.user("u1".userId)
    val user = User(uc.server, uc.id, "fake".topicId, Nil)
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
    val user = User(uc.server, uc.id, "fake".topicId, Nil)
    val ti = TopicInit(banner = "create and add")
    val tc = uc.topic("t1".topicId)
    val failure = GenerateIdFailed("topic", uc, "one" :: "two" :: "three" :: Nil)
    topicDao.createLocal _ expects ("u1".userId, ti) returning failure.left
    val res = topicOps.createTopic(user, ti)
    res shouldBe -\/(CreateTopicFailed(uc, ti, failure))
  }
}
