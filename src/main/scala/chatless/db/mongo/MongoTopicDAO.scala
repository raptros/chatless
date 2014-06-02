package chatless.db.mongo

import com.google.inject.Inject
import chatless.wiring.params.{ServerIdParam, TopicCollection}
import com.mongodb.casbah.Imports._
import chatless.model._
import chatless.model.ids._
import scalaz._
import scalaz.syntax.std.option._
import scalaz.syntax.std.boolean._
import scalaz.syntax.id._
import scalaz.std.list._
import scalaz.syntax.traverse._
import scalaz.syntax.bifunctor._
import scalaz.syntax.applicative._

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
    @TopicCollection val collection: MongoCollection,
    @ServerIdParam serverId: ServerCoordinate,
    idGenerator: IdGenerator)
  extends TopicDAO
  with LazyLogging
  with MongoSafe {

  import MongoSafe.{StringAsLocation, catchMongo, writeMongo}

  def get(coordinate: TopicCoordinate): DbError \/ Topic = for {
    res <- safeFindOne("topic" atCoord coordinate)(coordinate.query)
    dbo <- res \/> NoSuchObject(coordinate)
    topic <- dbo.decode[Topic] leftMap wrapDecodeErrors("topic", coordinate)
  } yield topic

  def find[A <% DBObject, B <% DBObject](ref: A, keys: B = DBO()) = collection.find(ref, keys).toIterable

  def listUserTopics(coordinate: UserCoordinate): DbResult[List[TopicCoordinate]] = for {
    res <- safeFindList("topics" atCoord coordinate)(coordinate.asBson, fields = DBO("id" :> 1))
    ids <- res.traverse[DbResult, TopicCoordinate] {
      _.field[String @@ TopicId]("id") leftMap wrapDecodeErrors("topic.id", coordinate) map coordinate.topic
    }
  } yield ids


  def insertUnique(topic: Topic) = writeMongo("topic", topic.coordinate.some) {
    val bson = topic.asBson
    collection.insert(bson)
    topic
  }

  def createLocal(user: String @@ UserId, init: TopicInit) = init.fixedId.fold {
    //when no id is provided, we can make several attempts to generate an ID and insert the topic.
    insertRetry(user, init, 3, Nil)
  } { id =>
    //when an ID is specified in the init, retry can't be attempted if it turns out to already exist
    initLocalTopic(user, id, init) |> insertUnique
  }

  private def insertRetry(user: String @@ UserId, init: TopicInit, tries: Int, tried: List[String]): DbResult[Topic] =
    if (tries <= 0)
      GenerateIdFailed("topic", serverId.user(user), tried).left
    else
      (idGenerator.nextTopicId() |> { initLocalTopic(user, _, init) } |> insertUnique) attemptLeft {
        case IdAlreadyUsed(c) => c.id.asInstanceOf[String]
      } thenTry { last =>
        insertRetry(user, init, tries - 1, last :: tried)
      }

  private def initLocalTopic(user: String @@ UserId, id: String @@ TopicId, init: TopicInit) =
    Topic(serverId.user(user).topic(id), init.banner, init.info, init.mode)

  def save(topic: Topic): DbResult[Topic] = for {
    wr <- writeMongo("topic" atCoord topic.coordinate) { collection.update(topic.coordinate.query, topic.asBson) }
    _ <- NoSuchObject(topic.coordinate).left unlessM wr.isUpdateOfExisting
  } yield topic

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

