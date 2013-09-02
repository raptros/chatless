package chatless.model

import chatless._

case class Event(eid: EventId)
  extends BaseModel

object Event {
  val EID = "eid"

}




