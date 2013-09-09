package chatless.events.model
import chatless._

import com.novus.salat.annotations._
import org.joda.time.DateTime

case class Event(
  @Key("_id") id: EventId,
  timestamp: DateTime,
  deltas: List[Delta])

object Event {
  val ID = "id"
  val TIMESTAMP = "timestamp"
  val DELTAS = "deltas"
}

