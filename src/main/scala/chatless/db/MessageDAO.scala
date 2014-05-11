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

  def rq(topic: TopicCoordinate, id: Option[String], forward: Boolean, inclusive: Boolean, count: Int): Iterable[Message]

  def first(topic: TopicCoordinate, count: Int = 1) = rq(topic, None, true, true, count)

  def last(topic: TopicCoordinate, count: Int = 1) = rq(topic, None, false, true, count)

  def at(topic: TopicCoordinate, id: String, count: Int = 1) = rq(topic, Some(id), false, true, count)

  def before(topic: TopicCoordinate, id: String, count: Int = 1) = rq(topic, Some(id), false, false, count)

  def from(topic: TopicCoordinate, id: String, count: Int = 1) = rq(topic, Some(id), true, true, count)

  def after(topic: TopicCoordinate, id: String, count: Int = 1) = rq(topic, Some(id), true, false, count)
}
