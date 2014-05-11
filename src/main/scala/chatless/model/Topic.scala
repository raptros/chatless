package chatless.model

import chatless._
import argonaut._
import Argonaut._

case class Topic(
    server: String,
    user: String,
    id: String,
    banner: String,
    info: Json)
  extends HasCoordinate[TopicCoordinate] {

  lazy val coordinate = TopicCoordinate(server, user, id)
}

object Topic {
  def apply(coord: TopicCoordinate, banner: String, info: Json): Topic = coord match {
    case TopicCoordinate(server, user, topic) => Topic(server, user, topic, banner, info)
  }

  implicit def topicCodecJson = casecodec5(Topic.apply, Topic.unapply)("server", "user", "id", "banner", "info")
}

