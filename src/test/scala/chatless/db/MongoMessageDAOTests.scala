package chatless.db
import org.scalatest.{FlatSpec, Matchers, fixture}
import scalaz._

import org.scalamock.scalatest._
import chatless.model._
import com.mongodb.casbah.Imports._
import chatless.db.mongo.{MessageCounterDAO, MongoMessageDAO, IdGenerator, MongoTopicDAO}
import scala.util.Random
import org.joda.time.DateTime
import chatless.MockFactory2

class MongoMessageDAOTests extends FlatSpec with Matchers with MockFactory2 {
  import scala.language.reflectiveCalls

  val mc = MongoClient()
  val testDB = mc("mongo-message-dao-test")

  val serverCoordinate = ServerCoordinate("fake-server")
  val userCoordinate = serverCoordinate.user("fake-user")
  val topicCoordinate = userCoordinate.topic("fake-topic")

  trait DbFixture {
    val collection: MongoCollection
    val dao: MongoMessageDAO
    val counterDao = mock[MessageCounterDAO]
    val idGen = mock[IdGenerator]
  }

  def withDb(test: DbFixture => Any) = {
    val coll = testDB(Random.nextString(10))
    val fixture = new DbFixture {
      val collection = coll
      val dao = new MongoMessageDAO(serverCoordinate, coll, counterDao, idGen)
    }
    try {
      test(fixture)
    } finally {
      coll.drop()
    }
  }

  behavior of "the mongo message dao"

  it should "be able to insert a unique message" in withDb { f =>
    val mc = topicCoordinate.message("test-insert-0")
    val mb = MessageBuilder(mc, DateTime.now())
    val message = mb.bannerChanged(userCoordinate, "updated banner")
    f.counterDao.inc _ expects topicCoordinate returning \/-(1l)
    val res = f.dao.insertUnique(message) valueOr { err => fail(s"insert failed! $err") }
    res should be (mc.idPart)
  }

  it should "be able to insert and get" in withDb { f =>
    val mc = topicCoordinate.message("test-insert-get-0")
    val mb = MessageBuilder(mc, DateTime.now())
    val message = mb.bannerChanged(userCoordinate, "updated banner")
    f.counterDao.inc _ expects topicCoordinate returning \/-(1l)
    val res = f.dao.insertUnique(message) valueOr { err => fail(s"insert failed! $err") }
    res should be (mc.idPart)
    val res2 = f.dao.get(mc) valueOr { err => fail(s"failed to get! $err") }
    res2 should be (message)
  }


}
