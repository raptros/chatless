package chatless.events
import chatless._
import com.google.inject.Inject
import chatless.db.EventDAO
import akka.actor.Actor
import chatless.model.Event
import akka.event.Logging
import scalaz._
import scalaz.syntax.id._
import scalaz.syntax.foldable._
import scalaz.std.option._
import scalaz.syntax.std.option._
import scalaz.std.list._

import shapeless._
import shapeless.Typeable._

class LocalEventReceiver @Inject() (
    val eventDao: EventDAO)
  extends Actor {

  val log = Logging(context.system, this)

  def saveEvent(e: Event): String \/ EventId = eventDao.add(e) <|  {
    case \/-(eid) => log.info("saved event {}", eid)
    case -\/(msg) => log.error("failed to save event {}: {}!", e, msg)
  }

  def receive = {
    case e: Event => saveEvent(e)
    case l: List[_] => handleList(l)
  }

  def saveEventsList(first:Event, rest: List[Event]) {
    (rest foldLeft saveEvent(first)) { (parent, next) =>
      parent flatMap { pId => saveEvent(next.copy(parent = pId.some)) }
    }
  }

  def handleList(l: List[Any]) {
    val eventsList = l flatMap { _.cast[Event] }
    if (eventsList.nonEmpty) {
      saveEventsList(eventsList.head, eventsList.tail)
    }
  }
}
