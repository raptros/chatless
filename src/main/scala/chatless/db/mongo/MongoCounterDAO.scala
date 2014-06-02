package chatless.db.mongo

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import chatless.wiring.params.CounterCollection
import chatless.model._
import chatless.db._

import scalaz.syntax.std.option._
import scalaz.syntax.validation._
import scalaz.syntax.id._

import scalaz.\/
import chatless.db.MissingCounter
import io.github.raptros.bson._
import Bson._
import chatless.db.WriteFailureWithCoordinate
import chatless.db.MissingCounter

class MongoCounterDAO @Inject() (@CounterCollection collection: MongoCollection) extends CounterDAO {

  def inc(purpose: String, coordinate: Coordinate): DbError \/ Long = for {
    res <- runOperation(purpose, coordinate) { atomicIncInner(counterId(purpose, coordinate)) }
    dbo <- res \/> MissingCounter(purpose, coordinate) //hopefully we won't run into these
    count <- dbo.field[Long]("counter") leftMap wrapDecodeErrors(s"$purpose-counter", coordinate)
  } yield count

  private def coordinateId(coordinate: Coordinate): String = coordinate match {
    case RootCoordinate => ""
    case ServerCoordinate(server) => server
    case UserCoordinate(server, user) => s"$server-$user"
    case TopicCoordinate(server, user, topic) => s"$server-$user-$topic"
    case MessageCoordinate(server, user, topic, message) => s"$server-$user-$topic-$message"
  }

  private def counterId(purpose: String, coordinate: Coordinate): String = s"ctr-$purpose-${coordinateId(coordinate)}"

  private def atomicIncInner(id: String) = collection.findAndModify(
    DBO("_id" :> id),
    fields = DBO("counter" :> 1),
    sort = DBO(),
    remove = false,
    update = $inc("counter" -> 1l),
    returnNew = true,
    upsert = true
  )

  @inline private def runOperation(purpose: String, coordinate: Coordinate)(inner: => Option[DBObject]) = try { inner.right } catch {
    case e: MongoException => WriteFailureWithCoordinate(s"ctr-$purpose", coordinate, e).left
  }
}
