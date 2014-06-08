package chatless.db

import org.scalatest.{FlatSpec, Matchers, fixture}
import scalaz._

import org.scalamock.scalatest.MockFactory
import chatless.model._
import chatless.model.ids._
import com.mongodb.casbah.Imports._
import chatless.db.mongo.{MongoTopicMemberDAO, IdGenerator, MongoTopicDAO}
import scala.util.Random
import chatless.MockFactory2
import chatless.model.topic.{MemberMode, TopicMode, TopicInit}
import argonaut._
import Argonaut._

class MongoTopicMemberDAOTests extends FlatSpec with Matchers {
  import scala.language.reflectiveCalls

  val mc = MongoClient()
  val testDB = mc("mongo-topic-dao-test")

  trait DbFixture {
    val collection: MongoCollection
    val dao: TopicMemberDAO
  }

  def withDb(test: DbFixture => Any) = {
    val coll = testDB(Random.alphanumeric.take(10).mkString)
    val fixture = new DbFixture {
      val collection = coll
      val dao = new MongoTopicMemberDAO(coll)
    }
    try {
      test(fixture)
    } finally {
      coll.drop()
    }
  }

  behavior of "the mongo topic member dao"

  it should "return none when no members" in withDb { f =>
    val topic = ServerCoordinate("s1".serverId).user("u1".userId).topic("t1".topicId)
    val user = ServerCoordinate("s2".serverId).user("u2".userId)
    val res = f.dao.get(topic, user) valueOr opFailed("get")
    res shouldBe empty
  }

  it should "insert a new member" in withDb { f =>
    val topic = ServerCoordinate("s1".serverId).user("u1".userId).topic("t1".topicId)
    val user = ServerCoordinate("s2".serverId).user("u2".userId)
    val mode = MemberMode.modeDeny
    val res = f.dao.set(topic, user, mode) valueOr opFailed("insert")
    res should have (
      'topic (topic),
      'user (user),
      'mode (mode)
    )
  }

  it should "not find a user in some other topic" in withDb { f =>
    val topic = ServerCoordinate("s1".serverId).user("u1".userId).topic("t1".topicId)
    val user = ServerCoordinate("s2".serverId).user("u2".userId)
    val mode = MemberMode.modeDeny
    val res = f.dao.set(topic, user, mode) valueOr opFailed("insert")
    val topic2 = ServerCoordinate("s1".serverId).user("u1".userId).topic("t2".topicId)
    val res2 = f.dao.get(topic2, user) valueOr opFailed("get")
    res2 shouldBe empty
  }

  it should "not pull in a user who is in the topic for one that isn't" in withDb { f =>
    val topic = ServerCoordinate("s1".serverId).user("u1".userId).topic("t1".topicId)
    val user = ServerCoordinate("s2".serverId).user("u2".userId)
    val mode = MemberMode.modeDeny
    val res = f.dao.set(topic, user, mode) valueOr opFailed("insert")
    val user2 = ServerCoordinate("s2".serverId).user("u3".userId)
    val res2 = f.dao.get(topic, user2) valueOr opFailed("get")
    res2 shouldBe empty
  }

  it should "insert and then get" in withDb { f =>
    val topic = ServerCoordinate("s1".serverId).user("u1".userId).topic("t1".topicId)
    val user = ServerCoordinate("s2".serverId).user("u2".userId)
    val mode = MemberMode.modeDeny
    val res = f.dao.set(topic, user, mode) valueOr opFailed("insert")
    res.mode shouldBe mode
    val member = f.dao.get(topic, user) valueOr opFailed("get")
    member should not be empty
    member.get should have (
      'topic (topic),
      'user (user),
      'mode (mode)
    )
  }

  it should "insert, get, and update" in withDb { f =>
    val topic = ServerCoordinate("s1".serverId).user("u1".userId).topic("t1".topicId)
    val user = ServerCoordinate("s2".serverId).user("u2".userId)
    val mode = MemberMode.modeDeny
    val res = f.dao.set(topic, user, mode) valueOr opFailed("insert")
    res.mode shouldBe mode
    val member = f.dao.get(topic, user) valueOr opFailed("get")
    member should not be empty
    member.get should have (
      'topic (topic),
      'user (user),
      'mode (mode)
    )
    val mode2 = MemberMode.modeDeny
    val res2 = f.dao.set(topic, user, mode2) valueOr opFailed("update")
    res2 should have (
      'topic (topic),
      'user (user),
      'mode (mode2)
    )
    //todo: don't rely on mongo
    f.collection.size shouldBe 1
    val member2 = f.dao.get(topic, user) valueOr opFailed("get")
    member2 should not be empty
    member2.get should have (
      'topic (topic),
      'user (user),
      'mode (mode2)
    )
  }

  it should "correctly list the members of a topic" in withDb { f =>
    val topic1 = TopicCoordinate("s1".serverId, "u1".userId, "t1".topicId)
    val topic2 = TopicCoordinate("s2".serverId, "u2".userId, "t2".topicId)
    val topic3 = TopicCoordinate("s2".serverId, "u2".userId, "t3".topicId)
    val user1 = UserCoordinate("s1".serverId, "u1".userId)
    val user2 = UserCoordinate("s2".serverId, "u2".userId)
    val user3 = UserCoordinate("s1".serverId, "u3".userId)
    val user4 = UserCoordinate("s2".serverId, "u4".userId)
    val user5 = UserCoordinate("s4".serverId, "u5".userId)
    val mode1 = MemberMode.modeDeny
    val mode2 = MemberMode.modeDeny
    val m1 = f.dao.set(topic1, user1, mode1) valueOr opFailed("insert")
    val m2 = f.dao.set(topic2, user2, mode1) valueOr opFailed("insert")
    val m3 = f.dao.set(topic1, user3, mode2) valueOr opFailed("insert")
    val m4 = f.dao.set(topic2, user4, mode2) valueOr opFailed("insert")
    val m5 = f.dao.set(topic1, user5, mode2) valueOr opFailed("insert")
    val l1 = f.dao.list(topic1) valueOr opFailed("list")
    l1 should contain only (m1, m3, m5)
    val l2 = f.dao.list(topic2) valueOr opFailed("list")
    l2 should contain only (m2, m4)
    val l3 = f.dao.list(topic3) valueOr opFailed("list")
    l3 shouldBe empty
  }

  private def opFailed(op: String): DbError => Nothing = err => fail(s"failed to $op: $err")
}
