package chatless.model.topic

import argonaut._
import Argonaut._

import chatless.macros._

import chatless.model.{TopicCoordinate, HasCoordinate}

case class Topic(
    server: String,
    user: String,
    id: String,
    banner: String,
    info: Json,
    mode: TopicMode)
  extends HasCoordinate[TopicCoordinate] {

  lazy val coordinate = TopicCoordinate(server, user, id)
}

object Topic {
  def apply(coord: TopicCoordinate, banner: String, info: Json, mode: TopicMode): Topic = coord match {
    case TopicCoordinate(server, user, topic) => Topic(server, user, topic, banner, info, mode)
  }

//  implicit def topicCodecJson = casecodec6(Topic.apply, Topic.unapply)("server", "user", "id", "banner", "info", "mode")
    implicit def topicCodecJson = JsonMacros.deriveCaseCodecJson[Topic]

}

