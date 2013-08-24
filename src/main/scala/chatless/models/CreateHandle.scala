package chatless.models
import chatless._

import org.joda.time.DateTime
import argonaut._
import Argonaut._

case class CreateHandle(id: String, timestamp: DateTime)

object CreateHandle {
  implicit val CreateHandleCodecJson =
    CodecJson.casecodec2(CreateHandle.apply _, CreateHandle.unapply _)("id", "timestamp")
}
