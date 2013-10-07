package chatless.db
import chatless._
import com.mongodb.casbah.Imports._
import chatless.model._
import scalaz._

trait EventDAO {
  def get(id: EventId): Option[Event]

  def add(event: Event): String \/ EventId

  def rq(user: User, id: Option[EventId], forward: Boolean, inclusive: Boolean, count: Int): Iterable[Event]

  def last(user: User, count: Int = 1) = rq(user, None, false, true, count)

  def at(user: User, id: EventId, count: Int = 1) = rq(user, Some(id), false, true, count)

  def before(user: User, id: EventId, count: Int = 1) = rq(user, Some(id), false, false, count)

  def from(user: User, id: EventId, count: Int = 1) = rq(user, Some(id), true, true, count)

  def after(user: User, id: EventId, count: Int = 1) = rq(user, Some(id), true, false, count)

}
