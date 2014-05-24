package chatless.db
import org.scalatest.{WordSpec, FlatSpec, Matchers, fixture}
import scalaz._

import org.scalamock.scalatest._
import chatless.model._
import com.mongodb.casbah.Imports._
import chatless.db.mongo._
import scala.util.Random
import org.joda.time.DateTime
import argonaut._
import Argonaut._
import chatless.MockFactory2
import scala.collection.immutable.IndexedSeq

class MongoMessageDAOTests extends WordSpec with Matchers with MockFactory2 {
  import scala.language.reflectiveCalls

  val mc = MongoClient()
  val testDB = mc("mongo-message-dao-test")

  val serverCoordinate = ServerCoordinate("fake-server")
  val userCoordinate = serverCoordinate.user("fake-user")

  trait DbFixture {
    val collection: MongoCollection
    val dao: MongoMessageDAO
    val idGen = mock[IdGenerator]
  }

  def withDb(test: DbFixture => Any) = {
    val coll = testDB(Random.alphanumeric.take(10).mkString)
    val counterColl = testDB("counter" + Random.alphanumeric.take(10).mkString)
    val fixture = new DbFixture {
      val collection = coll
      val counterDao = new MongoMessageCounterDAO(counterColl)
      val dao = new MongoMessageDAO(serverCoordinate, coll, counterDao, idGen)
    }
    try {
      test(fixture)
    } finally {
      coll.drop()
    }
  }
  def prepMessages(tc: TopicCoordinate, dao: MessageDAO): IndexedSeq[Message] =  (0 until 10) map { i =>
    val mc = tc.message(Random.alphanumeric take 5 append i.toString mkString "")
    val json = ("index" := i) ->: jEmptyObject
    val msg = MessageBuilder(mc, DateTime.now()).posted(userCoordinate, json)
    val insertRes = dao.insertUnique(msg) valueOr opFailed("insert")
    insertRes shouldBe mc.id
    msg
  }

  "the mongo message dao" should {
    "insert a unique message" in withDb { f =>
      val topicCoordinate = userCoordinate.topic("topic0")
      val mc = topicCoordinate.message("test-insert-0")
      val mb = MessageBuilder(mc, DateTime.now())
      val message = mb.bannerChanged(userCoordinate, "updated banner")
      val res = f.dao.insertUnique(message) valueOr opFailed("insert")
      res should be (mc.id)
    }

    "insert and get" in withDb { f =>
      val topicCoordinate = userCoordinate.topic("topic1")
      val mc = topicCoordinate.message("test-insert-get-0")
      val mb = MessageBuilder(mc, DateTime.now())
      val message = mb.bannerChanged(userCoordinate, "updated banner")
      val res = f.dao.insertUnique(message) valueOr opFailed("insert")
      res should be (mc.id)
      val res2 = f.dao.get(mc) valueOr opFailed("get")
      res2 should be (message)
    }
    "not insert" when {
      "the message collides with an existing id" in withDb { f =>
        val topicCoordinate = userCoordinate.topic("topic2")
        val mc = topicCoordinate.message("test-duplicate")
        val m1 = MessageBuilder(mc, DateTime.now()).bannerChanged(userCoordinate, "updated banner")
        val res1 = f.dao.insertUnique(m1) valueOr opFailed("insert")
        res1 should be (mc.id)
        val m2 = MessageBuilder(mc, DateTime.now()).posted(userCoordinate, jEmptyObject)
        val res2: (DbError \/ String) = f.dao.insertUnique(m2)
        res2 shouldBe -\/(IdAlreadyUsed(mc))
      }
    }
    "return an error" when {
      "an at query has a bad id" in withDb { f=>
        val tc = userCoordinate.topic("topic2")
        val msgs = prepMessages(tc, f.dao)
        //bad "at" case: fail if we request for a bad id
        f.dao.at(tc, "bogus", 2) shouldBe -\/(NoSuchObject(tc.message("bogus")))
      }
    }
    "return messages in order" when {
      "queried with 'first'" in withDb { f =>
        /*
        def first(topic: TopicCoordinate, count: Int = 1) =
          rq(topic, id = None, forward = true, inclusive = true, count = count)
        */
        val tc = userCoordinate.topic("topic2")
        val msgs = prepMessages(tc, f.dao)
        val res = f.dao.first(tc,3) valueOr opFailed("get")
        res.toStream should contain inOrderOnly (msgs(0), msgs(1), msgs(2))

        val res2 = f.dao.first(tc) valueOr opFailed("get")
        res2.toStream should contain only msgs(0)
      }
      "queried with 'last'" in withDb { f =>
        /*
        def last(topic: TopicCoordinate, count: Int = 1) =
          rq(topic, id = None, forward = false, inclusive = true, count = count)
        */
        val tc = userCoordinate.topic("topic2")
        val msgs = prepMessages(tc, f.dao)
        val res = f.dao.last(tc,3) valueOr opFailed("get")
        res.toStream should contain inOrderOnly (msgs(9), msgs(8), msgs(7))
      }
    }
    "return messages in the correct order and include the requested id" when {
      "queried with 'at'" in withDb { f =>
        /*
        def at(topic: TopicCoordinate, id: String, count: Int = 1) =
          rq(topic, id = Some(id), forward = false, inclusive = true, count = count)
        */
        val tc = userCoordinate.topic("topic2")
        val msgs = prepMessages(tc, f.dao)

        val res = f.dao.at(tc, msgs(2).id, 3) valueOr opFailed("get")
        res.toStream should contain inOrderOnly (msgs(2), msgs(1), msgs(0))

        val res2 = f.dao.at(tc, msgs(8).id) valueOr opFailed("get")
        res2.toStream should contain only msgs(8)

        val res3 = f.dao.at(tc, msgs(1).id, 3) valueOr opFailed("get")
        res3.toStream should contain inOrderOnly (msgs(1), msgs(0))
      }
      "queried with 'from'" in withDb { f =>
        /*
        def from(topic: TopicCoordinate, id: String, count: Int = 1) =
          rq(topic, id = Some(id), forward = true, inclusive = true, count = count)
        */
        val tc = userCoordinate.topic("topic2")
        val msgs = prepMessages(tc, f.dao)

        val res = f.dao.from(tc, msgs(5).id, 9) valueOr opFailed("get")
        res.toStream should contain inOrderOnly (msgs(5), msgs(6), msgs(7), msgs(8), msgs(9))

        val res2 = f.dao.from(tc, msgs(9).id) valueOr opFailed("get")
        res2.toStream should contain only msgs(9)

        val res3 = f.dao.from(tc, msgs(8).id, 3) valueOr opFailed("get")
        res3.toStream should contain inOrderOnly (msgs(8), msgs(9))
      }
    }
    "return messages in the correct order but exclude the one with the requested id" when {
      "queried with 'before'" in withDb { f =>
        /*
        def before(topic: TopicCoordinate, id: String, count: Int = 1) =
          rq(topic, id = Some(id), forward = false, inclusive = false, count = count)
         */
        val tc = userCoordinate.topic("topic2")
        val msgs = prepMessages(tc, f.dao)

        val res = f.dao.before(tc, msgs(5).id, 10) valueOr opFailed("get")
        res.toStream should contain inOrderOnly (msgs(4), msgs(3), msgs(2), msgs(1), msgs(0))

        val res2 = f.dao.before(tc, msgs(8).id) valueOr opFailed("get")
        res2.toStream should contain only msgs(7)

        val res3 = f.dao.before(tc, msgs(1).id, 2) valueOr opFailed("get")
        res3.toStream should contain only msgs(0)

        val res4 = f.dao.before(tc, msgs(0).id) valueOr opFailed("get")
        res4.toStream shouldBe empty

        val res5 = f.dao.before(tc, msgs(5).id, 3) valueOr opFailed("get")
        res5.toStream should contain inOrderOnly (msgs(4), msgs(3), msgs(2))
      }
      "queried with 'after'" in withDb { f =>
        /*
        def after(topic: TopicCoordinate, id: String, count: Int = 1) =
          rq(topic, id = Some(id), forward = true, inclusive = false, count = count)
        */
        val tc = userCoordinate.topic("topic2")
        val msgs = prepMessages(tc, f.dao)

        val res = f.dao.after(tc, msgs(4).id, 10) valueOr opFailed("get")
        res.toStream should contain inOrderOnly (msgs(5), msgs(6), msgs(7), msgs(8), msgs(9))

        val res2 = f.dao.after(tc, msgs(7).id) valueOr opFailed("get")
        res2.toStream should contain only msgs(8)

        val res3 = f.dao.after(tc, msgs(8).id, 2) valueOr opFailed("get")
        res3.toStream should contain only msgs(9)

        val res4 = f.dao.after(tc, msgs(9).id) valueOr opFailed("get")
        res4.toStream shouldBe empty

        val res5 = f.dao.after(tc, msgs(1).id, 3) valueOr opFailed("get")
        res5.toStream should contain inOrderOnly (msgs(2), msgs(3), msgs(4))
      }
    }
  }
  it when {
    "creating new messages" should {
      "successfully insert" in withDb { f =>
        val tc = userCoordinate.topic("topic3")
        f.idGen.nextMessageId _ expects() once() returning "fake1"
        val response = f.dao.createNew(MessageBuilder(tc).posted(userCoordinate, jEmptyObject).apply(""))
        val res = response valueOr opFailed("insert")
        res shouldBe "pst-fake1"
      }
      "retry and suceeed" in withDb { f =>
        val tc = userCoordinate.topic("topic3")
        inSequence {
          f.idGen.nextMessageId _ expects() twice() returning "fake1"
          f.idGen.nextMessageId _ expects() once() returning "fake2"
        }
        val response1 = f.dao.createNew(MessageBuilder(tc).posted(userCoordinate, jEmptyObject).apply(""))
        val res1 = response1 valueOr opFailed("insert")
        res1 shouldBe "pst-fake1"

        val response2 = f.dao.createNew(MessageBuilder(tc).posted(userCoordinate, jEmptyObject).apply(""))
        val res2 = response2 valueOr opFailed("insert")
        res2 shouldBe "pst-fake2"
      }
      "retry and give up" in withDb { f =>
        val tc = userCoordinate.topic("topic3")
        inSequence {
          f.idGen.nextMessageId _ expects() repeat 4 returning "fake1"
        }
        val response1 = f.dao.createNew(MessageBuilder(tc).posted(userCoordinate, jEmptyObject)(""))
        val res1 = response1 valueOr opFailed("insert")
        res1 shouldBe "pst-fake1"
        val response2 = f.dao.createNew(MessageBuilder(tc).posted(userCoordinate, jEmptyObject)(""))
        val res2 = response2.swap valueOr { x => fail(s"somehow inserted $x!") }
        res2 shouldBe a [GenerateIdFailed]
        val err = res2.asInstanceOf[GenerateIdFailed]
        err.what shouldBe "message"
        err.parent shouldBe tc
        err.attempted should contain only "pst-fake1"
        err.attempted should have length 3

      }
    }
  }

  private def opFailed(op: String): DbError => Nothing = err => fail(s"failed to $op: $err")
}
