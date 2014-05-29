package chatless.model

import chatless._
import argonaut._
import Argonaut._
import chatless.macros.JsonMacros

case class User(
    server: String,
    id: String,
    about: TopicId,
    pull: List[TopicCoordinate])
  extends HasCoordinate[UserCoordinate] {

  lazy val coordinate = UserCoordinate(server, id)
}

object User {
  implicit def userCodecJson = JsonMacros.deriveCaseCodecJson[User]
}

