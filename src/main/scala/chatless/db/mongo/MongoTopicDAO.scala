package chatless.db.mongo

import com.google.inject.Inject
import chatless.wiring.params.{ServerIdParam, TopicCollection}
import com.mongodb.casbah.Imports._
import chatless.model._
import scalaz._
import scalaz.syntax.std.option._
import scalaz.syntax.std.boolean._
import scalaz.syntax.id._

import argonaut._
import Argonaut._
import chatless.db._
import com.mongodb.DuplicateKeyException

import FilteredRetry._
import io.github.raptros.bson._
import Bson._
import codecs.Codecs._
import com.typesafe.scalalogging.slf4j.LazyLogging
import chatless.model.topic.{TopicInit, Topic}

class MongoTopicDAO @Inject() (
    @ServerIdParam serverId: ServerCoordinate,
    @TopicCollection collection: MongoCollection,
    idGenerator: IdGenerator)
  extends TopicDAO
  with LazyLogging {

  def get(coordinate: TopicCoordinate): DbError \/ Topic = for {
    dbo <- collection.findOne(coordinate.query) \/> NoSuchObject(coordinate)
    topic <- dbo.decode[Topic] leftMap { wrapDecodeErrors }
  } yield topic

  def find[A <% DBObject, B <% DBObject](ref: A, keys: B = DBO()) = collection.find(ref, keys).toIterable

  def listUserTopics(coordinate: UserCoordinate): Iterable[TopicCoordinate] = for {
    res <- find(coordinate.asBson, DBO("id" :> 1))
    id <- res.field[String]("id").toOption
  } yield coordinate.topic(id)


  def insertUnique(topic: Topic) = try {
    collection.insert(topic.asBson)
    topic.right
  } catch {
    case dup: DuplicateKeyException => IdAlreadyUsed(topic.coordinate).left
    case t: Throwable => WriteFailureWithCoordinate("topic", topic.coordinate, t).left
  }

  def createLocal(user: String, init: TopicInit) = init.fixedId.fold {
    //when no id is provided, we can make several attempts to generate an ID and insert the topic.
    insertRetry(user, init, 3, Nil)
  } { id =>
    //when an ID is specified in the init, retry can't be attempted if it turns out to already exist
    initLocalTopic(user, id, init) |> insertUnique
  }

  private def insertRetry(user: String, init: TopicInit, tries: Int, tried: List[String]): DbError \/ Topic =
    if (tries <= 0)
      GenerateIdFailed("topic", serverId.user(user), tried).left
    else
      (idGenerator.nextTopicId() |> { initLocalTopic(user, _, init) } |> insertUnique) attemptLeft {
        case IdAlreadyUsed(c) => c.id
      } thenTry { last =>
        insertRetry(user, init, tries - 1, last :: tried)
      }

  private def initLocalTopic(user: String, id: String, init: TopicInit) =
    Topic(serverId.user(user).topic(id), init.banner, init.info, init.mode)

  def save(topic: Topic): DbError \/ Topic = try {
    val writeResult = collection.update(topic.coordinate.query, topic.asBson)
    (!writeResult.isUpdateOfExisting) either NoSuchObject(topic.coordinate) or topic
  } catch {
    case e: MongoException => WriteFailureWithCoordinate("topic", topic.coordinate, e).left
  }

  private def setup() {
    logger.debug("setup()")
    //todo: indexes
    collection.ensureIndex(
      DBO("server" :> 1, "user" :> 1, "id" :> 1),
      DBO("unique" :> true)
    )
  }
  setup()

}

