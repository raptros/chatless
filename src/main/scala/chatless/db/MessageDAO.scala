package chatless.db

import chatless._
import chatless.model.Message
import scalaz.\/

trait MessageDAO {
  def get(id: MessageId): Option[Message]

  def saveNewMessage(message: Message): WriteStat

  def rq(tid: TopicId, id: Option[MessageId], forward: Boolean, inclusive: Boolean, count: Int): Iterable[Message]

  def first(tid: TopicId, count: Int = 1) = rq(tid, None, true, true, count)

  def last(tid: TopicId, count: Int = 1) = rq(tid, None, false, true, count)

  def at(tid: TopicId, id: EventId, count: Int = 1) = rq(tid, Some(id), false, true, count)

  def before(tid: TopicId, id: EventId, count: Int = 1) = rq(tid, Some(id), false, false, count)

  def from(tid: TopicId, id: EventId, count: Int = 1) = rq(tid, Some(id), true, true, count)

  def after(tid: TopicId, id: EventId, count: Int = 1) = rq(tid, Some(id), true, false, count)
}
