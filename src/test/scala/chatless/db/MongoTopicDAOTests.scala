package chatless.db
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpec, Matchers, fixture}
import spray.testkit.ScalatestRouteTest
import spray.routing._
import chatless._
import scalaz._

import org.scalamock.scalatest.MockFactory
import chatless.model._
import spray.http.MediaTypes._
import spray.httpx.Json4sSupport
import spray.httpx.marshalling.BasicMarshallers._
import spray.httpx.unmarshalling.BasicUnmarshallers._
import spray.http.{StatusCodes, StatusCode}
import akka.event.{LoggingAdapter, Logging}
import chatless.services.ClientApi
import akka.actor.ActorRefFactory
import com.mongodb.casbah.Imports._
import chatless.db.mongo.{IdGenerator, MongoTopicDAO}
import scala.util.Random
import com.mongodb.casbah.Imports

class MongoTopicDAOTests extends fixture.WordSpec
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



  "the mongo topic dao insert method" should {
    "successfully insert a new topic" when {
      "there is no conflict on the provided id" in { f =>
        val res = f.dao.createLocalTopic("user", TopicInit(fixedId = Some("test")))
        res should be (\/-("test"))
      }
      "it can generate an id" in { f =>
        f.idGen.nexts = List("testt")
        val res = f.dao.createLocalTopic("user", TopicInit())
        res should be (\/-("testt"))
      }
      "it can generate an id after retry" in { f=>
        f.idGen.nexts = List("t1", "t1", "t2")
        val res1 = f.dao.createLocalTopic("user", TopicInit())
        res1 should be (\/-("t1"))
        val res2 = f.dao.createLocalTopic("user", TopicInit())
        res2 should be (\/-("t2"))
      }
    }
  }

}
