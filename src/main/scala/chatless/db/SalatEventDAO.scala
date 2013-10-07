package chatless.db
import chatless._

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import chatless.model._
import chatless.wiring.params.EventCollection
import scalaz._
import scalaz.syntax.id._
import scalaz.syntax.std.option._
import com.mongodb.casbah.Imports

class SalatEventDAO @Inject()(
    @EventCollection collection: MongoCollection)
  extends SalatDAO[Event, String](collection)
  with EventDAO {

  def get(id: EventId): Option[Event] = findOneById(id)

  def add(event: Event): String \/ EventId = {
    val withId = event.copy(id = Some((new ObjectId).toStringMongod))
    val wr = save(withId)
    Option(wr.getError).toLeftDisjunction(withId.id.get)
  }

  private def idBasedQ(id: String, forward: Boolean, inclusive: Boolean) = (forward, inclusive) match {
    case (true, true)   => "_id" $gte id
    case (true, false)  => "_id" $gt id
    case (false, true)  => "_id" $lte id
    case (false, false) => "_id" $lt id
  }


  def rq(user: User, id: Option[String], forward: Boolean, inclusive: Boolean, count: Int): Iterable[Event] = {
    val filterQ = $or(
      $and(
        "kind" $eq EventKind.MESSAGE.id,
        "tid" $in user.topics),
      $and(
        "kind" $eq EventKind.TOPIC_UPDATE.id,
        "tid" $in user.topics),
      $and(
        "kind" $eq EventKind.USER_UPDATE.id,
        "uid" $in user.following))
    val mainQ = id map { i => $and(idBasedQ(i, forward, inclusive), filterQ) } getOrElse filterQ
    val cursor = find(mainQ) sort MongoDBObject("$natural" -> { if (forward) 1 else -1 }) limit count
    cursor.toIterable
  }


  def setup() {
    if (!collection.underlying.isCapped) {
      collection.getDB().command(MongoDBObject("convertToCapped" -> collection.name, "size" -> 500))
    }
    collection.ensureIndex("kind")
    //todo make some sparse indexes
    collection.ensureIndex("uid")
    collection.ensureIndex("tid")
  }

  setup()
}
