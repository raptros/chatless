package chatless.model

import chatless._
import argonaut._
import Argonaut._
import chatless.macros.JsonMacros
import scalaz._
import ids._

case class User(
    server: String @@ ServerId,
    id: String @@ UserId,
    about: String @@ TopicId,
    invites: String @@ TopicId,
    pull: List[TopicCoordinate])
  extends HasCoordinate[UserCoordinate] {

  lazy val coordinate = UserCoordinate(server, id)
}

object User {
  implicit def userCodecJson = JsonMacros.deriveCaseCodecJson[User]
}

