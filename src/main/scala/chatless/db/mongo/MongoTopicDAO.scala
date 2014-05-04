package chatless.db.mongo

import com.google.inject.Inject
import chatless.wiring.params.{ServerIdParam, TopicCollection}
import com.mongodb.casbah.Imports._
import chatless.model._
import scalaz._
import scalaz.syntax.std.option._
import scalaz.syntax.validation._
import scalaz.syntax.id._

import argonaut._
import Argonaut._
import com.mongodb.casbah.commons.NotNothing
import chatless.db._
import com.mongodb.DuplicateKeyException
import chatless.db.DeserializationErrors
import scala.annotation.tailrec

class MongoTopicDAO @Inject() (
    @ServerIdParam serverId: ServerCoordinate,
    @TopicCollection collection: MongoCollection,
    idGenerator: IdGenerator)
  extends TopicDAO {

  import MongoTopicDAO._

  def getTopic(coordinate: TopicCoordinate): DbError \/ Topic = for {
    dbo <- collection.findOne(topicCoordAsQuery(coordinate)) \/> NoSuchObject(coordinate)
    topic <- extractTopic(dbo).disjunction leftMap { nel => DeserializationErrors(nel.list) }
  } yield topic

  def find[A <% DBObject, B <% DBObject](ref: A, keys: B = MongoDBObject()) = collection.find(ref, keys).toIterable

  def listUserTopics(coordinate: UserCoordinate): Iterable[TopicCoordinate] =
    find(MDBO(Fields.server -> coordinate.server, Fields.user -> coordinate.user), MongoDBObject()) flatMap {
      _.extractKey[String](Fields.id).toOption
    } map { coordinate.topic }


  def insertUniqueTopic(topic: Topic) = {
    try {
      collection.insert(marshalTopic(topic))
      topic.id.right
    } catch {
      case dup: DuplicateKeyException => IdAlreadyUsed(topic.coordinate).left
      case t: Throwable => WriteFailure(t).left
    }
  }

  def createLocalTopic(user: String, init: TopicInit) = init.fixedId.fold {
    //when no id is provided, we can make several attempts to generate an ID and insert the topic.
    val id = idGenerator.nextTopicId()
    val localTopic = initLocalTopic(user, id, init)
    attemptInsertAndRetry(localTopic, 3, Nil)
  } { id =>
    //when an ID is specified in the init, retry can't be attempted if it turns out to already exist
    initLocalTopic(user, id, init) |> insertUniqueTopic
  }

  private def attemptInsertAndRetry(topic: Topic, tries: Int, tried: List[String]): DbError \/ String =
    if (tries <= 0) GenerateIdFailed("topic", topic.coordinate.parent, tried).left
    else insertUniqueTopic(topic) whenLeft {
      _.isInstanceOf[IdAlreadyUsed]
    } thenTry {
      attemptInsertAndRetry(topic.copy(id = idGenerator.nextTopicId()), tries - 1, topic.id :: tried)
    }

  private def initLocalTopic(user: String, id: String, init: TopicInit) =
    Topic(serverId.user(user).topic(id), init.banner, init.info)

  private def setup() {
    //todo: indexes
    collection.ensureIndex(
      MDBO(Fields.server -> 1, Fields.user -> 1, Fields.id -> 1),
      MongoDBObject("unique" -> true)
    )
  }

  setup()
}

object MongoTopicDAO {

  def extractTopic(dbo: DBObject): ValidationNel[String, Topic] = ApV.apply5(
    dbo.validateKeyNel[String](Fields.server),
    dbo.validateKeyNel[String](Fields.user),
    dbo.validateKeyNel[String](Fields.id),
    dbo.validateKeyNel[String](Fields.banner),
    getInfo(dbo).validation.toValidationNel
  )(Topic.apply)

  private def getInfo(dbo: DBObject) = dbo.extractKey[String](Fields.info) flatMap { Parse.parse } leftMap { msg =>
    s"could not parse info: $msg" 
  }
  
  def marshalTopic(topic: Topic) = MDBO(
    //ensure that _id collision has exactly the same behavior as coordinate collision
    Fields._id -> (topic.server + topic.user + topic.id),
    Fields.server -> topic.server,
    Fields.user -> topic.user,
    Fields.id -> topic.id,
    Fields.banner -> topic.banner,
    Fields.info -> topic.info.asJson.nospaces
  )

  def topicCoordAsQuery(coord: TopicCoordinate) = MDBO(
    Fields.server -> coord.server,
    Fields.user -> coord.user,
    Fields.id -> coord.topic
  )

}
