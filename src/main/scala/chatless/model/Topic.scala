package chatless.model

import chatless._
import argonaut._
import Argonaut._

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
  implicit def topicCodecJson = casecodec5(Topic.apply, Topic.unapply)("server", "user", "id", "banner", "info")
}

