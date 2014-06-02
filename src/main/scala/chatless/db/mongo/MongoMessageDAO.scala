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
import scalaz.syntax.traverse._
import scalaz.std.list._

import io.github.raptros.bson._
import Bson._
import codecs.Codecs._

import com.mongodb.DuplicateKeyException
import com.mongodb.casbah.query.dsl.QueryExpressionObject
import FilteredRetry._
import com.typesafe.scalalogging.slf4j.LazyLogging
import chatless.model.ids._

class MongoMessageDAO @Inject() (
    @MessageCollection val collection: MongoCollection,
    @ServerIdParam serverId: ServerCoordinate,
    counterDao: CounterDAO,
    idGenerator: IdGenerator)
  extends MessageDAO
  with LazyLogging
  with MongoSafe {

  import MongoSafe.{StringAsLocation, writeMongo}

  def get(coord: MessageCoordinate) = for {
    res <- safeFindOne("message" atCoord coord)(coord.query)
    dbo <- res \/> NoSuchObject(coord)
    msg <- dbo.decode[Message] leftMap wrapDecodeErrors("messsage", coord)
  } yield msg

  def insertUnique(message: Message): DbResult[String @@ MessageId] = for {
    nextPos <- counterDao.inc("msgs", message.coordinate.parent)
    id <- insertUniqueInner(message, nextPos)
  } yield id

  private def insertUniqueInner(message: Message, pos: Long): DbResult[String @@ MessageId] =
    writeMongo("message" atCoord message.coordinate) {
      val messageDBO = message.asBson +@+ ("pos" :> pos)
      collection.insert(messageDBO)
      message.id
    }

  def createNew(m: Message): DbResult[String @@ MessageId] = insertRetry(m, 3, Nil)

  private def insertRetry(m: Message, tries: Int, tried: List[String]): DbResult[String @@ MessageId] =
    if (tries <= 0)
      GenerateIdFailed("message", m.coordinate.parent, tried).left
    else
      (idGenerator.nextMessageId() |> { m.change(_) } |> insertUnique) attemptLeft  {
        case IdAlreadyUsed(c) => c.id.asInstanceOf[String]
      } thenTry { last =>
        insertRetry(m, tries - 1, last :: tried)
      }

  def rq(topic: TopicCoordinate, id: Option[String @@ MessageId], forward: Boolean, inclusive: Boolean, count: Int): DbResult[Iterable[Message]] = for {
    query <- buildQuery(topic.asBson, forward, inclusive) { id map { topic.message } }
    //todo: what about streaming instead?
    dbos <- safeFindList("messages" atCoord topic)(query, limit = count.some, orderBy = DBO("pos" :> (forward ? 1 | -1)))
    msgs <- dbos.traverse[DbResult, Message] { _.decode[Message] leftMap wrapDecodeErrors("message", topic) }
  } yield msgs

  private def buildQuery(baseQ: DBObject, forward: Boolean, inclusive: Boolean)
                        (omc: Option[MessageCoordinate]): DbResult[DBObject] =
    (omc fold baseQ.right[DbError]) { getPositionQuery(baseQ, forward, inclusive) }

  private def getPositionQuery(baseQ: DBObject, forward: Boolean, inclusive: Boolean)(mc: MessageCoordinate) = for {
    //find the message object at the coordinate, and get the position field from it
    res <- safeFindOne("message" atCoord mc)(mc.query, DBO("pos" :> 1))
    dbo <- res \/> NoSuchObject(mc)
    //get the position and turn it into a query
    posQ <- dbo.field[Long]("pos") leftMap wrapDecodeErrors("message.pos", mc) map relMatch(forward, inclusive)
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

