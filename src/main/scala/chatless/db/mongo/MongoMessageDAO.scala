package chatless.db.mongo

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import chatless.wiring.params.{ServerIdParam, MessageCollection}
import chatless.model._
import chatless.db._

import scalaz.syntax.std.option._
import scalaz.syntax.std.boolean._
import scalaz._
import scalaz.syntax.id._
import com.osinka.subset._


import chatless.db.mongo.builders._
import chatless.db.mongo.parsers._
import com.mongodb.DuplicateKeyException
import chatless.db.mongo.builders.CoordinateAsQuery
import com.mongodb.casbah.query.dsl.QueryExpressionObject

class MongoMessageDAO @Inject() (
    @ServerIdParam serverId: ServerCoordinate,
    @MessageCollection collection: MongoCollection,
    counterDao: MessageCounterDAO,
    idGenerator: IdGenerator)
  extends MessageDAO {

  def get(coord: MessageCoordinate) = for {
    dbo <- collection.findOne(coord.asQuery) \/> NoSuchObject(coord)
    message <- dbo.parseAs[Message]
  } yield message

  /** attempts to insert the message as a unique message
    * @param message a message
    * @return id or failure
    */
  override def insertUnique(message: Message): DbError \/ String = for {
    nextPos <- counterDao.inc(message.coordinate.parent)
    id <- insertUniqueInner(message, nextPos)
  } yield id

  private def insertUniqueInner(message: Message, pos: Long): DbError \/ String = try {
    val messageDBO = WritesToDBOWrapper(message).getBuffer.attach(Fields.pos --> pos).apply()
    collection.insert(messageDBO)
    message.id.right
  } catch {
    case dup: DuplicateKeyException => IdAlreadyUsed(message.coordinate).left
    case t: Throwable => WriteFailureWithCoordinate("message", message.coordinate, t).left
  }

  def rq(topic: TopicCoordinate, id: Option[String], forward: Boolean, inclusive: Boolean, count: Int): DbError \/ Iterable[Message] = for {
    q <- buildQuery(topic.getDBO: DBObject, forward, inclusive) { id map { topic.message } }
    cursor = runRQ(q, forward, count)
    stream = parseMessages(cursor)
  } yield stream

  private def parseMessages(cursor: MongoCursor): Iterable[Message] = cursor.toStream flatMap { dbo =>
    val parseRes = dbo.parseAs[Message]
    cursor.size
    parseRes leftMap { err => "" } //todo logging
    parseRes.toStream
  }

  private def runRQ(q: DBObject, forward: Boolean, count: Int): MongoCursor =
    collection.find(q) sort DBO2(Fields.pos --> (forward ? 1 | -1))() limit count

  private def buildQuery(baseQ: DBObject, forward: Boolean, inclusive: Boolean)
                        (omc: Option[MessageCoordinate]): DbError \/ DBObject =
    (omc fold ToIdOps(baseQ).right[DbError]) { getPositionQuery(baseQ, forward, inclusive) }

  private def getPositionQuery(baseQ: DBObject, forward: Boolean, inclusive: Boolean)(mc: MessageCoordinate) = for {
    //find the message object at the coordinate, and get the position field from it
    position <- collection.findOne(mc.asQuery, fields = DBO2(Fields.pos --> 1)()) \/> NoSuchObject(mc)
    //get the position and turn it into a query
    posQ <- position.getField[Long](Fields.pos) map { relMatch(forward, inclusive) }
  } yield $and(baseQ, posQ)

  /** takes forward, inclusive, and then position */
  private val relMatch: (Boolean, Boolean) => Long => (DBObject with QueryExpressionObject) = {
    case (true, true) => Fields.pos.toString $gte _
    case (true, false) => Fields.pos.toString $gt _
    case (false, true) => Fields.pos.toString $lte _
    case (false, false) => Fields.pos.toString $lt _
  }

  private def setup() {
    //todo: indexes
    collection.ensureIndex(
      DBO2(Fields.server --> 1, Fields.user --> 1, Fields.topic --> 1, Fields.id --> 1)(),
      DBO("unique" -> true)()
    )
    collection.ensureIndex(
      DBO2(Fields.server --> 1, Fields.user --> 1, Fields.topic --> 1, Fields.pos --> 1)(),
      DBO("unique" -> true)()
    )
  }

  setup()
}

