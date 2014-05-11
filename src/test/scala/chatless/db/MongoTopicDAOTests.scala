package chatless.db
import org.scalatest.{Matchers, fixture}
import scalaz._

import org.scalamock.scalatest.MockFactory
import chatless.model._
import com.mongodb.casbah.Imports._
import chatless.db.mongo.{IdGenerator, MongoTopicDAO}
import scala.util.Random

class MongoTopicDAOTests extends fixture.FlatSpec
with Matchers
with MockFactory {
  import scala.language.reflectiveCalls

  val mc = MongoClient()
  val testDB = mc("mongo-topic-dao-test")

  trait FixtureParam {
    val collection: MongoCollection
    val dao: MongoTopicDAO
    val idGen = new IdGenerator {
      var nexts: List[String] = Nil
      def nextTopicId(): String = if (nexts.nonEmpty) {
        val n = nexts.head
        nexts = nexts.tail
        n
      } else throw new Exception("nextTopicId() probably wasn't supposed to be called")

    }
    val serverCoordinate: ServerCoordinate = ServerCoordinate("fake")
  }

  def withFixture(test: OneArgTest) = {
    val coll = testDB(Random.nextString(10))
    val fixture = new FixtureParam {
      val collection = coll
      val dao = new MongoTopicDAO(serverCoordinate, coll, idGen)
    }
    try {
      withFixture(test.toNoArgTest(fixture))
    } finally {
      coll.drop()
    }
  }

  behavior of "the mongo topic dao"

  it should "successfully insert a new topic" in { f =>
    val res = f.dao.createLocal("user", TopicInit(fixedId = Some("test")))
    res should be (\/-("test"))
  }

  it should "generate an id and insert a new topic" in { f=>
    f.idGen.nexts = List("testt")
    val res = f.dao.createLocal("user", TopicInit())
    res should be (\/-("testt"))
  }

  it should "retry the generate and insert successfully" in { f=>
    f.idGen.nexts = List("t1", "t1", "t2")
    val res1 = f.dao.createLocal("user", TopicInit())
    res1 should be (\/-("t1"))
    val res2 = f.dao.createLocal("user", TopicInit())
    res2 should be (\/-("t2"))
  }

  it should "insert and then get a topic" in { f =>
    val res = f.dao.createLocal("user", TopicInit(fixedId = Some("insert1")))
    res should be (\/-("insert1"))
    val res2 = f.dao.get(f.serverCoordinate.user("user").topic("insert1"))
    val loadedTopic = res2.fold(err => fail(s"got some kind of error! $err"), identity)
    loadedTopic should have (
      'id ("insert1"),
      'user ("user")
    )
  }
}
