package chatless.db.mongo

import com.mongodb.casbah.Imports._
import chatless.model.Coordinate
import chatless.db.{ReadFailure, DbError, ReadFailureWithCoordinate, DbResult}
import scalaz.syntax.id._
import scalaz.syntax.std.boolean._
import scalaz.\/

trait MongoSafe {
  import MongoSafe._

  def collection: MongoCollection

  def safeFindOne(q: DBObject, where: Location): DbResult[Option[DBObject]] =
    readMongo(where) {  collection.findOne(q) }

  /** fail-early */
  def safeFindList(q: DBObject, where: Location): DbResult[List[DBObject]] =
    readMongo(where) { collection.find(q).toList }

  type DbStream = Stream[DbResult[DBObject]]

  /** fail-late */
  def safeFindStream(q: DBObject, where: Location): DbStream =  streamCursor(collection.find(q), where)

  private def streamCursor(c: MongoCursor, where: Location): DbStream =
    readMongo(where) { c.next() } #::  continueStreamingCursor(c, where)

  private def continueStreamingCursor(c: MongoCursor, where: Location): DbStream =
    readMongo(where) { c.hasNext } leftMap {
      _.left #:: Stream.empty[DbResult[DBObject]]
    } map {
      _ ? streamCursor(c, where) | Stream.empty[DbResult[DBObject]]
    } valueOr identity

}

object MongoSafe {
  type Location = (String, Option[Coordinate])

  def readMongo[A](where: Location)(op: => A): DbError \/ A = catchMongo(op) leftMap asReadFailure(where)

  def asReadFailure(where: Location): MongoException => DbError = where match {
    case (what, Some(c)) => e => ReadFailureWithCoordinate(what, c, e)
    case (what, None) => e => ReadFailure(what, e)
  }

  def catchMongo[A](op: => A): MongoException \/ A = try { op.right } catch {
    case e: MongoException => e.left
  }

  implicit class StringAsLocation(what: String) {
    def alone: Location = what -> None

    def atCoord(c: Coordinate): Location = what -> Some(c)
  }
}
