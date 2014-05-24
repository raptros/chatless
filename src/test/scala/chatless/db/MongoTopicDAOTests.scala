package chatless.db
import org.scalatest.{FlatSpec, Matchers, fixture}
import scalaz._

import org.scalamock.scalatest.MockFactory
import chatless.model._
import com.mongodb.casbah.Imports._
import chatless.db.mongo.{IdGenerator, MongoTopicDAO}
import scala.util.Random
import chatless.MockFactory2
import chatless.model.topic.{TopicMode, TopicInit}
import argonaut._
import Argonaut._

class MongoTopicDAOTests extends FlatSpec with Matchers with MockFactory2 with TopicMatchers {
  import scala.language.reflectiveCalls

  val mc = MongoClient()
  val testDB = mc("mongo-topic-dao-test")

  trait DbFixture {
    val collection: MongoCollection
    val dao: MongoTopicDAO
    val idGen = mock[IdGenerator]
    val serverCoordinate: ServerCoordinate = ServerCoordinate("fake")
    val uc = serverCoordinate.user("user")
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
    val res = f.dao.createLocal("user", TopicInit(fixedId = Some("test"))) valueOr opFailed("create", f.uc.topic("test"))
    res should have (
      id ("test"),
      user ("user")
    )
  }

  it should "generate an id and insert a new topic" in withDb { f=>
    f.idGen.nextTopicId _ expects () returning "testt"
    val res = f.dao.createLocal(f.uc.id, TopicInit()) valueOr opFailed("create", f.uc.topic("testt"))
    res should have (
      id ("testt")
    )
  }

  it should "retry the generate and insert successfully" in withDb { f=>
    inSequence {
      f.idGen.nextTopicId _ expects () returning "t1"
      f.idGen.nextTopicId _ expects () returning "t2"
    }
    val res1 = f.dao.createLocal(f.uc.id, TopicInit()) valueOr opFailed("create", f.uc)
    res1 should have (
      id ("t1")
    )
    val res2 = f.dao.createLocal(f.uc.id, TopicInit()) valueOr opFailed("create", f.uc)
    res2 should have (
      id ("t2")
    )
  }

  it should "retry 3 times and give up" in withDb { f =>
    f.idGen.nextTopicId _ expects () repeat 4 returning "t1"
    val res1 = f.dao.createLocal(f.uc.id, TopicInit()) valueOr opFailed("create", f.uc)
    res1 should have (
      id ("t1")
    )
    val res2 = ~f.dao.createLocal(f.uc.id, TopicInit()) valueOr { x => fail(s"somehow managed to insert $x") }
    res2 shouldBe a [GenerateIdFailed]
    val err = res2.asInstanceOf[GenerateIdFailed]
    err.what shouldBe "topic"
    err.parent shouldBe f.uc
    err.attempted should have length 3
    err.attempted should contain only "t1"
  }

  it should "insert and then get a topic" in withDb { f =>
    val tc = f.uc.topic("insert1")
    val res = f.dao.createLocal(f.uc.id, TopicInit(fixedId = Some(tc.id))) valueOr opFailed("create", f.uc)
    res should have (
      coordinate (tc)
    )
    val res2 = f.dao.get(tc) valueOr opFailed("load", tc)
    res2 should have (
      coordinate (tc)
    )
  }

  it should "save changes to a topic" in withDb { f =>
    val tc = f.uc topic "updates"
    val res = f.dao.createLocal(f.uc.id, TopicInit(fixedId = Some(tc.id))) valueOr opFailed("insert", f.uc)
    res should have (
      coordinate (tc)
    )
    val first = f.dao.get(tc) valueOr opFailed("get", tc)
    first should have (
      coordinate (tc),
      banner (""),
      info (jEmptyObject),
      mode (TopicMode.default)
    )
    val newMode: TopicMode = TopicMode(public = true, open = true, muted = true)
    val second = first.copy(banner = "test banner 1", mode = newMode)
    val secondSaved = f.dao.save(second) valueOr opFailed("save", tc)
    secondSaved shouldBe second
    val third = f.dao.get(tc) valueOr opFailed("get", tc)
    third should have (
      coordinate (tc),
      banner ("test banner 1"),
      info (jEmptyObject),
      mode (newMode)
    )
  }

  private def opFailed(op: String, c: Coordinate): DbError => Nothing = err => fail(s"failed to $op at $c: $err")
}
