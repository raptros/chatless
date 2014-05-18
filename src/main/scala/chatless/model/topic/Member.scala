package chatless.model.topic

import chatless.model.{UserCoordinate, TopicCoordinate}

case class Member(
  topic: TopicCoordinate,
  user: UserCoordinate,
  mode: MemberMode)

