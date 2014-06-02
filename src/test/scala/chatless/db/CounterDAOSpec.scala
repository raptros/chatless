package chatless.db

import org.scalatest.{Matchers, FlatSpec}
import chatless.MockFactory2
import com.mongodb.casbah.Imports._
import chatless.db.mongo.{CounterDAO, MongoCounterDAO}
import chatless.model.{TopicCoordinate, UserCoordinate}
import scala.util.Random
import chatless.model.ids._

class CounterDAOSpec extends FlatSpec with Matchers with MockFactory2 {
  import scala.language.reflectiveCalls

  val mc = MongoClient()
  val testDB = mc("mongo-counter-dao-test")

  trait DbFixture {
    val collection: MongoCollection
    val dao: MongoCounterDAO
    val userCoord = UserCoordinate("fake".serverId, "test".userId)
  }

  def withDb(test: DbFixture => Any) = {
    val coll = testDB(Random.nextString(10))
    val fixture = new DbFixture {
      val collection = coll
      val dao = new MongoCounterDAO(coll)
    }
    try {
      test(fixture)
    } finally {
      coll.drop()
    }
  }

  def incOrFail(dao: CounterDAO, tc: TopicCoordinate) = {
    dao.inc("test", tc) valueOr { err => fail(s"failed to increment for $tc: $err")}
  }

  behavior of "the mongo message counter dao"

  it should "increment once" in withDb { f =>
    val res = incOrFail(f.dao, f.userCoord.topic("test1".topicId))
    res shouldBe 1l
  }

  it should "increment two things separately" in withDb { f=>
    incOrFail(f.dao, f.userCoord.topic("test2-a".topicId)) shouldBe 1l
    incOrFail(f.dao, f.userCoord.topic("test2-b".topicId)) shouldBe 1l
    incOrFail(f.dao, f.userCoord.topic("test2-a".topicId)) shouldBe 2l
  }

  it should "increment a hundred times properly" in withDb { f =>
    for (i <- 0 until 100) {
      incOrFail(f.dao, f.userCoord.topic("test3".topicId)) shouldBe (i + 1).toLong
    }
  }

  it should "increment two separate counters properly 100 times" in withDb { f=>
    val t1 = f.userCoord.topic("test4-a".topicId)
    val t2 = f.userCoord.topic("test4-b".topicId)
    incOrFail(f.dao, t1) shouldBe 1l
    incOrFail(f.dao, t1) shouldBe 2l
    incOrFail(f.dao, t2) shouldBe 1l
    incOrFail(f.dao, t1) shouldBe 3l
    for (i <- 0 until 100) {
      incOrFail(f.dao, t1) shouldBe (i + 4).toLong
      incOrFail(f.dao, t2) shouldBe (i + 2).toLong
    }
  }

}
