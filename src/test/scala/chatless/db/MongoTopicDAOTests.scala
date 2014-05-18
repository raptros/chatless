package chatless.db
import org.scalatest.{FlatSpec, Matchers, fixture}
import scalaz._

import org.scalamock.scalatest.MockFactory
import chatless.model._
import com.mongodb.casbah.Imports._
import chatless.db.mongo.{IdGenerator, MongoTopicDAO}
import scala.util.Random
import chatless.MockFactory2
import chatless.model.topic.TopicInit

class MongoTopicDAOTests extends FlatSpec with Matchers with MockFactory2 {
  import scala.language.reflectiveCalls

  val mc = MongoClient()
  val testDB = mc("mongo-topic-dao-test")

  trait DbFixture {
    val collection: MongoCollection
    val dao: MongoTopicDAO
    val idGen = mock[IdGenerator]
    val serverCoordinate: ServerCoordinate = ServerCoordinate("fake")
  }

  def withDb(test: DbFixture => Any) = {
    val coll = testDB(Random.alphanumeric.take(10).mkString)
    val fixture = new DbFixture {
      val collection = coll
      val dao = new MongoTopicDAO(serverCoordinate, coll, idGen)
    }
    try {
      test(fixture)
    } finally {
      coll.drop()
    }
  }

  behavior of "the mongo topic dao"

  it should "successfully insert a new topic" in withDb { f =>
    val res = f.dao.createLocal("user", TopicInit(fixedId = Some("test")))
    res should be (\/-("test"))
  }

  it should "generate an id and insert a new topic" in withDb { f=>
    f.idGen.nextTopicId _ expects () returning "testt"
    val res = f.dao.createLocal("user", TopicInit())
    res should be (\/-("testt"))
  }

  it should "retry the generate and insert successfully" in withDb { f=>
    inSequence {
      f.idGen.nextTopicId _ expects () returning "t1"
      f.idGen.nextTopicId _ expects () returning "t2"
    }
    val res1 = f.dao.createLocal("user", TopicInit())
    res1 should be (\/-("t1"))
    val res2 = f.dao.createLocal("user", TopicInit())
    res2 should be (\/-("t2"))
  }
  it should "retry 3 times and give up" in withDb { f =>
    f.idGen.nextTopicId _ expects () repeat 4 returning "t1"
    val res1 = f.dao.createLocal("user", TopicInit())
    res1 should be (\/-("t1"))
    val res2 = ~f.dao.createLocal("user", TopicInit()) valueOr { x => fail(s"somehow managed to insert $x") }
    res2 shouldBe a [GenerateIdFailed]
    val err = res2.asInstanceOf[GenerateIdFailed]
    err.what shouldBe "topic"
    err.parent shouldBe f.serverCoordinate.user("user")
    err.attempted should have length 3
    err.attempted should contain only "t1"
  }

  it should "insert and then get a topic" in withDb { f =>
    val res = f.dao.createLocal("user", TopicInit(fixedId = Some("insert1")))
    res should be (\/-("insert1"))
    val res2 = f.dao.get(f.serverCoordinate.user("user").topic("insert1"))
    val loadedTopic = res2 valueOr { err => fail(s"got some kind of error! $err") }
    loadedTopic should have (
      'id ("insert1"),
      'user ("user")
    )
  }
}
