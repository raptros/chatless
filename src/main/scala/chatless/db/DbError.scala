package chatless.db

import chatless.model.{Coordinate, UserCoordinate, TopicCoordinate}

sealed trait DbError {
  def isServerError: Boolean
}

trait ServerError extends DbError {
  val isServerError = true
}

trait RequestError extends DbError {
  val isServerError = false
}

case class IdAlreadyUsed(coord: Coordinate) extends RequestError

case class GenerateIdFailed(parent: Coordinate, attempted: List[String]) extends ServerError

case class WriteFailure(t: Throwable) extends ServerError

case class DeserializationErrors(messages: List[String]) extends ServerError

case class NoSuchTopic(topic: TopicCoordinate) extends RequestError

