package chatless.db.mongo

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import chatless.wiring.params.CounterCollection
import chatless.model.{Coordinate, TopicCoordinate}
import chatless.db._

import scalaz.syntax.std.option._
import scalaz.syntax.validation._
import scalaz.syntax.id._

import scalaz.\/
import chatless.db.MissingCounter
import io.github.raptros.bson._
import Bson._

class MongoMessageCounterDAO @Inject() (@CounterCollection collection: MongoCollection) extends MessageCounterDAO {

  def inc(topic: TopicCoordinate): DbError \/ Long = for {
    res <- runOperation(topic) { atomicIncInner(topicCounterId(topic)) }
    dbo <- res \/> MissingCounter(topic) //hopefully we won't run into these
    count <- dbo.field[Long]("counter") leftMap { wrapDecodeErrors }
  } yield count

  private def topicCounterId(coordinate: TopicCoordinate) =
    s"ctr-${coordinate.server}-${coordinate.user}-${coordinate.topic}"

  private def atomicIncInner(id: String) = collection.findAndModify(
    DBO("_id" :> id),
    fields = DBO("counter" :> 1),
    sort = DBO(),
    remove = false,
    update = $inc("counter" -> 1l),
    returnNew = true,
    upsert = true
  )

  @inline private def runOperation(coordinate: Coordinate)(inner: => Option[DBObject]) = try { inner.right } catch {
    case e: MongoException => WriteFailureWithCoordinate("counter", coordinate, e).left
  }
}
