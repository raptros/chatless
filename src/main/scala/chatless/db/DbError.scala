package chatless.db

import chatless.model.{Coordinate, UserCoordinate, TopicCoordinate}

sealed trait DbError {
}

case class IdAlreadyUsed(coord: Coordinate) extends DbError

case class GenerateIdFailed(what: String, parent: Coordinate, attempted: List[String]) extends DbError

case class WriteFailure(t: Throwable) extends DbError

case class DeserializationErrors(messages: List[String]) extends DbError

case class NoSuchObject(c: Coordinate) extends DbError
