package chatless.models
import chatless._
import argonaut._
import Argonaut._

case class Event(eid: EventId)

object Event {
  val EID = "eid"

  implicit def EventCodecJ: CodecJson[Event] =
    CodecJson.casecodec1(Event.apply, Event.unapply)(EID)
}

