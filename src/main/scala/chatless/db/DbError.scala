package chatless.db

import chatless.model.{Coordinate, UserCoordinate, TopicCoordinate}

sealed trait DbError {
}

case class IdAlreadyUsed(coord: Coordinate) extends DbError

case class GenerateIdFailed(what: String, parent: Coordinate, attempted: List[String]) extends DbError

case class WriteFailure(what: String, t: Throwable) extends DbError {
  def addCoordinate(coordinate: Coordinate) = WriteFailureWithCoordinate(what, coordinate, t)
}

case class WriteFailureWithCoordinate(what: String, coordinate: Coordinate, t: Throwable) extends DbError

case class DeserializationErrors(messages: List[String]) extends DbError

case class NoSuchObject(c: Coordinate) extends DbError

case class MissingCounter(purpose: String, coordinate: Coordinate) extends DbError

case class ReadFailure(what: String, t: Throwable) extends DbError {
  def addCoordinate(coordinate: Coordinate) = ReadFailureWithCoordinate(what, coordinate, t)
}

case class ReadFailureWithCoordinate(what: String, coordinate: Coordinate, t: Throwable) extends DbError