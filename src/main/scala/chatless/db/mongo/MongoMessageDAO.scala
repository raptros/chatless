package chatless.db.mongo

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import chatless.wiring.params.{ServerIdParam, MessageCollection}
import chatless.model._
import chatless.db._

import scalaz.syntax.std.option._
import scalaz.\/
import com.osinka.subset.DBO
import chatless.db.NoSuchObject

import BuilderUtils._
import Serializers._
import chatless.db.mongo.parsers._

class MongoMessageDAO @Inject() (
    @ServerIdParam serverId: ServerCoordinate,
    @MessageCollection collection: MongoCollection,
    counterDao: MongoMessageCounterDAO,
    idGenerator: IdGenerator)
  extends MessageDAO {

  def get(coord: MessageCoordinate) = for {
    dbo <- collection.findOne(coordinateQuery(coord)) \/> NoSuchObject(coord)
    message <- dbo.parseAs[Message]
  } yield message

  /** attempts to insert the message as a unique message
    * @param message a message
    * @return id or failure
    */
  override def insertUnique(message: Message): DbError \/ String = ???

  override def rq(topic: TopicCoordinate, id: Option[String], forward: Boolean, inclusive: Boolean, count: Int): Iterable[Message] = ???

  private def setup() {
    //todo: indexes
    collection.ensureIndex(
      DBO2(Fields.server -> 1, Fields.user -> 1, Fields.topic -> 1, Fields.id -> 1)(),
      DBO("unique" -> true)()
    )
    collection.ensureIndex(
      DBO2(Fields.server -> 1, Fields.user -> 1, Fields.topic -> 1, Fields.pos -> 1)(),
      DBO("unique" -> true)()
    )
  }

  setup()
}

