package chatless.db

import chatless._
import chatless.model._
import chatless.wiring.params.MessageCollection

import com.novus.salat.dao._
import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import scalaz.\/.fromTryCatch
import com.mongodb.casbah.query.dsl.QueryExpressionObject

class SalatMessageDAO @Inject()(
    @MessageCollection collection: MongoCollection,
    val counterDao: CounterDAO)
  extends SalatDAO[Message, String](collection)
  with MessageDAO {

  def get(id: MessageId) = findOneById(id)

  def saveNewMessage(message: Message)  = for {
    nextPos <- counterDao.next(TOPIC_MESSAGE_SEQUENCE_ID(message.tid))
    positionalMessage = message.copy(pos = Some(nextPos))
    //both casbah and salat have this annoying tendency to not document that it throws exception in various situations.
    savedId <- fromTryCatch { insert(positionalMessage) } leftMap { _.getMessage }
  } yield savedId.nonEmpty && savedId.get == positionalMessage.id

  private type PQMaker = Long => DBObject with QueryExpressionObject

  private def positionalQ(forward: Boolean, inclusive: Boolean): PQMaker = (forward, inclusive) match {
    case (true, true)   => Message.POSITION.$gte[Long]
    case (true, false)  => Message.POSITION.$gt[Long]
    case (false, true)  => Message.POSITION.$lte[Long]
    case (false, false) => Message.POSITION.$lt[Long]
  }

  /** this should be fairly fast because both the position and the id are in the index, so it can handle the query */
  private def getPosition(id: String) = collection.findOneByID(id, MongoDBObject(Message.POSITION -> 1)) flatMap {
    _.getAs[Long](Message.POSITION)
  }

  private def addIdQuery(id: Option[String], forward: Boolean, inclusive: Boolean) = id flatMap { getPosition } map {
    positionalQ(forward, inclusive)
  }

  @inline private def joinOpt(q: DBObject, oq: Option[DBObject]) = oq map { $and(_, q) } getOrElse q

  def rqInner(tid: TopicId, id: Option[MessageId], forward: Boolean, inclusive: Boolean, count: Int) = find {
    joinOpt(Message.TID $eq tid, addIdQuery(id, forward, inclusive))
  } sort MongoDBObject(
    Message.POSITION -> { if (forward) 1 else -1 }
  ) limit count

  def rq(tid: TopicId, id: Option[MessageId], forward: Boolean, inclusive: Boolean, count: Int) =
    rqInner(tid, id, forward, inclusive, count).toIterable

  def setup() {
    collection.ensureIndex(MongoDBObject(Message.TID -> 1, Message.ID -> 1))
    collection.ensureIndex(MongoDBObject(Message.ID -> 1))
    collection.ensureIndex(MongoDBObject(Message.ID -> 1, Message.POSITION -> 1))
  }
}
