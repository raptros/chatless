package chatless.db.mongo

import com.mongodb.casbah.Imports._
import chatless.model.Coordinate
import chatless.db._
import scalaz.syntax.id._
import scalaz.syntax.std.boolean._
import scalaz.\/
import io.github.raptros.bson.Bson._
import chatless.db.ReadFailureWithCoordinate
import chatless.db.ReadFailure
import scala.Some
import com.mongodb.DuplicateKeyException
import com.typesafe.scalalogging.Logging

trait MongoSafe { this: Logging =>
  import MongoSafe._

  def collection: MongoCollection

  def safeFindOne(where: Location)(q: DBObject, fields: DBObject = DBO()): DbResult[Option[DBObject]] =
    readMongo(where) {  collection.findOne(q) }

  /** fail-early */
  def safeFindList(where: Location)(q: DBObject, limit: Option[Int] = None, fields: DBObject = DBO(), orderBy: DBObject = DBO()): DbResult[List[DBObject]] =
    readMongo(where) {
      val cursor = collection.find(q, fields).sort(orderBy)
      (limit fold cursor)(cursor.limit).toList
    }

  type DbStream = Stream[DbResult[DBObject]]

  /** fail-late */
  def safeFindStream(where: Location)(q: DBObject, fields: DBObject = DBO(), orderBy: DBObject = DBO(), limit: Option[Int] = None): DbStream =
    streamCursor(where) {
      val cursor1 = collection.find(q, fields).sort(orderBy)
      (limit fold cursor1)(cursor1.limit)
    }

  private def streamCursor(where: Location)(c: MongoCursor): DbStream =
    readMongo(where) { c.next() } #::
      readMongo(where)(c.hasNext).fold(endOnError, wrapContinue(streamCursor(where)(c)))

  private val endOnError: DbError => DbStream = _.left #:: Stream.empty[DbResult[DBObject]]

  private def wrapContinue(op: => DbStream): Boolean => DbStream = _ ? op | Stream.empty[DbResult[DBObject]]

}

object MongoSafe {
  type Location = (String, Option[Coordinate])

  //todo: add logging
  def readMongo[A](where: Location)(op: => A): DbResult[A] = catchMongo(op) leftMap asReadFailure(where)

  //todo: add logging
  def writeMongo[A](where: Location)(op: => A): DbResult[A] = catchMongo(op) leftMap asWriteFailure(where)

  def asReadFailure(where: Location): MongoException => DbError = where match {
    case (what, Some(c)) => e => ReadFailureWithCoordinate(what, c, e)
    case (what, None) => e => ReadFailure(what, e)
  }

  /** wraps up a mongo exception in an appropriate Db Error for a write at the location.
    * this is usually a WriteFailure or WriteFailureWithCoordinate, though if a coordinate is provided and the exception
    * is a DuplicateKeyException, this returns an IdAlreadyUsed on the provided coordinate
    * @param where a Location - a string explaining what is being written to and an optional coordinate
    * @return a function from a mongo exception to the appropriate DbError related to writing at the location.
    */
  def asWriteFailure(where: Location): MongoException => DbError = where match {
    case (what, Some(c)) => {
      case dup: DuplicateKeyException => IdAlreadyUsed(c)
      case e: MongoException => WriteFailureWithCoordinate(what, c, e)
    }
    case (what, None) => e => WriteFailure(what, e)
  }

  def catchMongo[A](op: => A): MongoException \/ A = try { op.right } catch {
    case e: MongoException => e.left
  }

  implicit class StringAsLocation(what: String) {
    def alone: Location = what -> None

    def atCoord(c: Coordinate): Location = what -> Some(c)
  }
}
