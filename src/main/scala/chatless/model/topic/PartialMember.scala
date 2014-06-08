package chatless.model.topic

import chatless.model.{UserCoordinate, TopicCoordinate}
import argonaut._
import chatless.macros.JsonMacros

case class PartialMember(
  user: UserCoordinate,
  mode: MemberMode)


object PartialMember {
  implicit val partialMemberCodecJson = JsonMacros.deriveCaseCodecJson[PartialMember]
}
