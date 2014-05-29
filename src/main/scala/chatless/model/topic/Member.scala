package chatless.model.topic

import chatless.model.{UserCoordinate, TopicCoordinate}
import argonaut._
import chatless.macros.JsonMacros

case class Member(
  topic: TopicCoordinate,
  user: UserCoordinate,
  mode: MemberMode)

object Member {
  implicit val memberCodecJson = JsonMacros.deriveCaseCodecJson[Member]
}

