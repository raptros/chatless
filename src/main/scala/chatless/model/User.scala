package chatless.model

import chatless._
import argonaut._
import Argonaut._

case class User(
    server: ServerId,
    id: UserId,
    about: TopicId,
    pull: List[TopicCoordinate]
  ) extends HasCoordinate[UserCoordinate] {

  lazy val coordinate = UserCoordinate(server, id)
}

object User {
  implicit def userCodecJson = casecodec4(User.apply, User.unapply)("server", "id", "about", "pull")
}

