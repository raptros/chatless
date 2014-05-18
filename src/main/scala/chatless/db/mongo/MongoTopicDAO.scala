package chatless.db.mongo

import com.google.inject.Inject
import chatless.wiring.params.{ServerIdParam, TopicCollection}
import com.mongodb.casbah.Imports._
import chatless.model._
import scalaz._
import scalaz.syntax.std.either._
import scalaz.syntax.std.option._
import scalaz.syntax.id._

import argonaut._
import Argonaut._
import com.mongodb.casbah.commons.NotNothing
import chatless.db._
import com.mongodb.DuplicateKeyException

import com.osinka.subset._
import builders._
import FilteredRetry._

import parsers._
import com.typesafe.scalalogging.Logging
import com.typesafe.scalalogging.slf4j.LazyLogging
import chatless.model.topic.{TopicInit, Topic}

class MongoTopicDAO @Inject() (
    @ServerIdParam serverId: ServerCoordinate,
    @TopicCollection collection: MongoCollection,
    idGenerator: IdGenerator)
  extends TopicDAO
  with LazyLogging {

  def get(coordinate: TopicCoordinate): DbError \/ Topic = for {
    dbo <- collection.findOne(coordinate.asQuery) \/> NoSuchObject(coordinate)
    topic <- dbo.parseAs[Topic]
  } yield topic

  def find[A <% DBObject, B <% DBObject](ref: A, keys: B = MongoDBObject()) = collection.find(ref, keys).toIterable

  def listUserTopics(coordinate: UserCoordinate): Iterable[TopicCoordinate] = for {
    res <- find(coordinate.getDBO: DBObject, MongoDBObject())
    id <- res.getField[String](Fields.id).toOption
  } yield coordinate.topic(id)


  def insertUnique(topic: Topic) = try {
    collection.insert(topic.getDBO)
    topic.id.right
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

  private def insertRetry(user: String, init: TopicInit, tries: Int, tried: List[String]): DbError \/ String =
    if (tries <= 0)
      GenerateIdFailed("topic", serverId.user(user), tried).left
    else
      (idGenerator.nextTopicId() |> { initLocalTopic(user, _, init) } |> insertUnique) attemptLeft {
        case IdAlreadyUsed(c) => c.idPart
      } thenTry { last =>
        insertRetry(user, init, tries - 1, last :: tried)
      }

  private def initLocalTopic(user: String, id: String, init: TopicInit) =
    Topic(serverId.user(user).topic(id), init.banner, init.info, init.mode)

  private def setup() {
    logger.debug("setup()")
    //todo: indexes
    collection.ensureIndex(
      DBO2(Fields.server --> 1, Fields.user --> 1, Fields.id --> 1)(),
      DBO("unique" -> true)()
    )
  }

  setup()
}

