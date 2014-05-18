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

  /** creates a new message
    * @param m a message - the ID of this Message object will be discarded
    * @return if not an error, the ID of the created message
    */
  def createNew(m: Message): DbError \/ String

  /** relative query
    * @param topic the coordinate of the topic to query in
    * @param id optionally, the id of a message to base the result set around
    * @param forward whether to get messages going forward in history (as oposed to backwards)
    * @param inclusive whether to include the message used at the starting point (only meaningful if id is non-empty)
    * @param count how many messages to include in the result set
    * @return if no errors, a stream of messages fulfilling the query
    */
  def rq(topic: TopicCoordinate, id: Option[String], forward: Boolean, inclusive: Boolean, count: Int): DbError \/ Iterable[Message]

  /** gets the first `count` messages in the topic
    * @param topic the coordinate of the topic to query in
    * @param count how many messages to include in the result set
    * @return rq(topic, id = None, forward = true, inclusive = true, count = count)
    */
  def first(topic: TopicCoordinate, count: Int = 1) =
    rq(topic, id = None, forward = true, inclusive = true, count = count)

  /** gets the last `count` messages in the topic
    * @param topic the coordinate of the topic to query in
    * @param count how many messages to include in the result set
    * @return rq(topic, id = None, forward = false, inclusive = true, count = count)
    */
  def last(topic: TopicCoordinate, count: Int = 1) =
    rq(topic, id = None, forward = false, inclusive = true, count = count)

  /** gets a message and the `count` minus one messages that came before it
    * @param topic the coordinate of the topic to query in
    * @param id the id of the message to base the query around
    * @param count how many messages to include in the result set
    * @return rq(topic, id = Some(id), forward = false, inclusive = true, count = count)
    */
  def at(topic: TopicCoordinate, id: String, count: Int = 1) =
    rq(topic, id = Some(id), forward = false, inclusive = true, count = count)

  /** gets the `count` messages in the topic that came before a particular message
    * @param topic the coordinate of the topic to query in
    * @param id the id of the message to base the query around
    * @param count how many messages to include in the result set
    * @return rq(topic, id = Some(id), forward = false, inclusive = false, count = count)
    */
  def before(topic: TopicCoordinate, id: String, count: Int = 1) =
    rq(topic, id = Some(id), forward = false, inclusive = false, count = count)

  /** gets a message and the `count` minus one messages that came after it
    * @param topic the coordinate of the topic to query in
    * @param id the id of the message to base the query around
    * @param count how many messages to include in the result set
    * @return rq(topic, id = Some(id), forward = true, inclusive = true, count = count)
    */
  def from(topic: TopicCoordinate, id: String, count: Int = 1) =
    rq(topic, id = Some(id), forward = true, inclusive = true, count = count)

  /** gets the `count` messages in the topic that came after a particular message
    * @param topic the coordinate of the topic to query in
    * @param id the id of the message to base the query around
    * @param count how many messages to include in the result set
    * @return rq(topic, id = Some(id), forward = true, inclusive = false, count = count)
    */
  def after(topic: TopicCoordinate, id: String, count: Int = 1) =
    rq(topic, id = Some(id), forward = true, inclusive = false, count = count)
}
