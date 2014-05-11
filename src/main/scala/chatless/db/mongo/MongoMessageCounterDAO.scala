package chatless.db.mongo

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import chatless.wiring.params.CounterCollection
import chatless.model.TopicCoordinate
import chatless.db.{DeserializationErrors, MissingCounter, WriteFailure}

import scalaz.syntax.std.option._
import scalaz.syntax.validation._
import scalaz.syntax.id._

import BuilderUtils._
import com.osinka.subset._
import parsers._

class MongoMessageCounterDAO @Inject() (@CounterCollection collection: MongoCollection) {

  def topicCounterId(coordinate: TopicCoordinate) =
    s"ctr-${coordinate.server}-${coordinate.user}-${coordinate.topic}"

  /** returns the value of a counter for messages in a topic
    * @param topic a topic coordinate to have a counter for
    * @return the number of times the counter has been incremented (the first call will get 1), or failure
    */
  def inc(topic: TopicCoordinate) = for {
    res <- runOperation { atomicIncInner(topicCounterId(topic)) } leftMap { _.addCoordinate(topic) }
    dbo <- res \/> MissingCounter(topic) //hopefully we won't run into these
    count <- dbo.getField[Long](Fields.counter)
  } yield count

  private def atomicIncInner(id: String) = collection.findAndModify(
    DBO2(Fields._id -> id)(),
    fields = DBO2(Fields.counter -> 1)(),
    sort = MongoDBObject(),
    remove = false,
    update = $inc(Fields.counter.toString -> 1l),
    returnNew = true,
    upsert = true
  )

  @inline private def runOperation(inner: => Option[DBObject]) = try { inner.right } catch {
    case e: MongoException => WriteFailure("counter", e).left
  }
}
