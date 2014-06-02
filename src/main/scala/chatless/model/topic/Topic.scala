package chatless.model.topic

import argonaut._
import Argonaut._
import scalaz._
import chatless.model._
import ids._

import chatless.macros._

case class Topic(
    server: String @@ ServerId,
    user: String @@ UserId,
    id: String @@ TopicId,
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

  implicit def topicCodecJson = JsonMacros.deriveCaseCodecJson[Topic]
}

