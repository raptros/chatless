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

import io.github.raptros.bson._
import Bson._
import codecs.Codecs._

import com.mongodb.DuplicateKeyException
import com.mongodb.casbah.query.dsl.QueryExpressionObject
import FilteredRetry._
import com.typesafe.scalalogging.slf4j.LazyLogging

class MongoMessageDAO @Inject() (
    @ServerIdParam serverId: ServerCoordinate,
    @MessageCollection collection: MongoCollection,
    counterDao: CounterDAO,
    idGenerator: IdGenerator)
  extends MessageDAO
  with LazyLogging {

  def get(coord: MessageCoordinate) = for {
    dbo <- collection.findOne(coord.query) \/> NoSuchObject(coord)
    message <- dbo.decode[Message] leftMap { wrapDecodeErrors }
  } yield message

  def insertUnique(message: Message): DbError \/ String = for {
    nextPos <- counterDao.inc("msgs", message.coordinate.parent)
    id <- insertUniqueInner(message, nextPos)
  } yield id

  private def insertUniqueInner(message: Message, pos: Long): DbError \/ String = try {
    val messageDBO = message.asBson +@+ ("pos" :> pos)
    collection.insert(messageDBO)
    message.id.right
  } catch {
    case dup: DuplicateKeyException => IdAlreadyUsed(message.coordinate).left
    case t: Throwable => WriteFailureWithCoordinate("message", message.coordinate, t).left
  }

  def createNew(m: Message): DbError \/ String = insertRetry(m, 3, Nil)

  private def insertRetry(m: Message, tries: Int, tried: List[String]): DbError \/ String =
    if (tries <= 0)
      GenerateIdFailed("message", m.coordinate.parent, tried).left
    else
      (idGenerator.nextMessageId() |> { m.change(_) } |> insertUnique) attemptLeft  {
        case IdAlreadyUsed(c) => c.id
      } thenTry { last =>
        insertRetry(m, tries - 1, last :: tried)
      }

  def rq(topic: TopicCoordinate, id: Option[String], forward: Boolean, inclusive: Boolean, count: Int): DbError \/ Iterable[Message] = for {
    q <- buildQuery(topic.asBson, forward, inclusive) { id map { topic.message } }
    cursor = runRQ(q, forward, count)
    stream = parseMessages(cursor)
  } yield stream

  private def parseMessages(cursor: MongoCursor): Iterable[Message] = cursor.toStream flatMap { dbo =>
    val parseRes = dbo.decode[Message] leftMap { wrapDecodeErrors }
    cursor.size
    parseRes leftMap { err => logger.error("could not parse message: {}", err) }
    parseRes.toStream
  }

  //todo: what if find throws an exception?
  private def runRQ(q: DBObject, forward: Boolean, count: Int): MongoCursor =
    collection.find(q) sort DBO("pos" :> (forward ? 1 | -1)) limit count

  private def buildQuery(baseQ: DBObject, forward: Boolean, inclusive: Boolean)
                        (omc: Option[MessageCoordinate]): DbError \/ DBObject =
    (omc fold baseQ.right[DbError]) { getPositionQuery(baseQ, forward, inclusive) }

  private def getPositionQuery(baseQ: DBObject, forward: Boolean, inclusive: Boolean)(mc: MessageCoordinate) = for {
    //find the message object at the coordinate, and get the position field from it
    position <- collection.findOne(mc.query, fields = DBO("pos" :> 1)) \/> NoSuchObject(mc)
    //get the position and turn it into a query
    posQ <- position.field[Long]("pos") leftMap { wrapDecodeErrors } map { relMatch(forward, inclusive) }
  } yield $and(baseQ, posQ)

  /** takes forward, inclusive, and then position */
  private val relMatch: (Boolean, Boolean) => Long => (DBObject with QueryExpressionObject) = {
    case (true, true) => "pos" $gte _
    case (true, false) => "pos" $gt _
    case (false, true) => "pos" $lte _
    case (false, false) => "pos" $lt _
  }

  private def setup() {
    logger.debug("setup()")
    //todo: indexes
    collection.ensureIndex(
      DBO("server" :> 1, "user" :> 1, "topic" :> 1, "id" :> 1),
      DBO("unique" :> true)
    )
    collection.ensureIndex(
      DBO("server" :> 1, "user" :> 1, "topic" :> 1, "pos" :> 1),
      DBO("unique" :> true)
    )
  }
  setup()


}

