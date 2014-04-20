package chatless.model

import chatless._
import argonaut.Json

case class Topic(
    server: ServerId,
    user: UserId,
    id:     TopicId,
    banner:  String,
    info:   Json)
  extends HasCoordinate[TopicCoordinate] {

  lazy val coordinate = TopicCoordinate(server, user, id)
}

object Topic {
}

