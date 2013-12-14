package chatless.db
import chatless._

import com.novus.salat.dao._
import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import chatless.model._
import chatless.wiring.params.EventCollection
import scalaz._
import scalaz.\/.fromTryCatch
import org.joda.time.DateTime
import com.mongodb.casbah.query.dsl.QueryExpressionObject
import com.mongodb.casbah.commons.Imports
import com.mongodb.casbah

class SalatEventDAO @Inject()(
    @EventCollection collection: MongoCollection,
    val counterDao: CounterDAO)
  extends SalatDAO[Event, String](collection)
  with EventDAO {

  def get(id: EventId): Option[Event] = findOneById(id)

  def add(event: Event): String \/ EventId = for {
    nextPos <- counterDao.next(EVENT_SEQUENCE_ID)
    withId = event.copy(id = Some((new ObjectId).toStringMongod), pos = Some(nextPos))
    //both casbah and salat have this annoying tendency to not document that it throws exception in various situations.
    _ <- fromTryCatch { save(withId) } leftMap { _.getMessage }
  } yield withId.id.get

  private type PQMaker = Long => DBObject with QueryExpressionObject

  private def positionalQ(forward: Boolean, inclusive: Boolean): PQMaker = (forward, inclusive) match {
    case (true, true)   => Event.POSITION.$gte[Long]
    case (true, false)  => Event.POSITION.$gt[Long]
    case (false, true)  => Event.POSITION.$lte[Long]
    case (false, false) => Event.POSITION.$lt[Long]
  }

  /** this should be fairly fast because both the position and the id are in the index, so it can handle the query */
  private def getPosition(id: String) = collection.findOneByID(id, MongoDBObject(Event.POSITION -> 1)) flatMap {
    _.getAs[Long](Event.POSITION)
  }

  private def addIdQuery(id: Option[String], forward: Boolean, inclusive: Boolean) = id flatMap { getPosition } map {
    positionalQ(forward, inclusive)
  }

  @inline private def joinOpt(q: DBObject, oq: Option[DBObject]) = oq map { $and(_, q) } getOrElse q

  private def mkFilterQuery(user: User) = $or(
    $and(
      "kind" $eq EventKind.MESSAGE.id,
      "tid" $in user.topics),
    $and(
      "kind" $eq EventKind.TOPIC_UPDATE.id,
      "tid" $in user.topics),
    $and(
      "kind" $eq EventKind.USER_UPDATE.id,
      "uid" $eq user.id),
    $and(
      "kind" $eq EventKind.USER_UPDATE.id,
      "uid" $in user.following)
  )

  private def rqInner(user: User, id: Option[String], forward: Boolean, inclusive: Boolean, count: Int) = find {
    joinOpt(mkFilterQuery(user), addIdQuery(id, forward, inclusive))
  } sort MongoDBObject(
    Event.POSITION -> { if (forward) 1 else -1 }
  ) limit count

  def rq(user: User, id: Option[String], forward: Boolean, inclusive: Boolean, count: Int) =
    rqInner(user, id, forward, inclusive, count).toIterable

  def oldestKnownEventTime = primitiveProjection[DateTime](
    MongoDBObject(
      "$query" -> (Event.TIMESTAMP $exists true),
      "$orderby" -> MongoDBObject(Event.POSITION -> 1)),
    Event.TIMESTAMP)

  def setup() {
    collection.ensureIndex(Event.POSITION)
    collection.ensureIndex(Event.KIND)
    //todo make some sparse indexes
    collection.ensureIndex(Event.TIMESTAMP)
    collection.ensureIndex(Event.UID)
    collection.ensureIndex(Event.TID)
  }

  setup()
}
