package chatless.model

import argonaut._
import Argonaut._

case class TopicInit(
  fixedId: Option[String] = None,
  banner: String = "",
  info: Json = Json.obj(),
  invite: List[UserCoordinate] = Nil)


object TopicInit {
  implicit def topicInitCodecJson = casecodec4(TopicInit.apply, TopicInit.unapply)("fix-id", "banner", "info", "invite")
}
