package chatless.db

import chatless._
import chatless.model.{TopicCoordinate, MessageCoordinate, Message}
import scalaz.\/

trait MessageDAO {
  /** attempts to get the message at the provided coordinate.
    * @param coord where the message is expected to be
    * @return either the message or an error
    */
  def get(coord: MessageCoordinate): DbError \/ Message

  /** attempts to insert the message as a unique message
    * @param message a message
    * @return id or failure
    */
  def insertUnique(message: Message): DbError \/ String

  def rq(topic: TopicCoordinate, id: Option[String], forward: Boolean, inclusive: Boolean, count: Int): DbError \/ Iterable[Message]

  def first(topic: TopicCoordinate, count: Int = 1) =
    rq(topic, id = None, forward = true, inclusive = true, count = count)

  def last(topic: TopicCoordinate, count: Int = 1) =
    rq(topic, id = None, forward = false, inclusive = true, count = count)

  def at(topic: TopicCoordinate, id: String, count: Int = 1) =
    rq(topic, id = Some(id), forward = false, inclusive = true, count = count)

  def before(topic: TopicCoordinate, id: String, count: Int = 1) =
    rq(topic, id = Some(id), forward = false, inclusive = false, count = count)

  def from(topic: TopicCoordinate, id: String, count: Int = 1) =
    rq(topic, id = Some(id), forward = true, inclusive = true, count = count)

  def after(topic: TopicCoordinate, id: String, count: Int = 1) =
    rq(topic, id = Some(id), forward = true, inclusive = false, count = count)
}
