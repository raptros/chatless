package chatless.model

import org.joda.time.DateTime
import argonaut._
import Argonaut._

case class MessageBuilder(server: String, user: String, topic: String, message: String, timestamp: DateTime) {
  def posted(poster: UserCoordinate, body: Json) =
    PostedMessage(server, topic, user, message, timestamp,poster, body)

  val postedT = (posted _).tupled

  def bannerChanged(poster: UserCoordinate, banner: String) =
    BannerChangedMessage(server, topic, user, message, timestamp, poster, banner)

  val bannerChangedT = (bannerChanged _).tupled

  def userJoined(joined: UserCoordinate) =
    UserJoinedMessage(server, topic, user, message, timestamp, joined)
}

object MessageBuilder {
  import MessageFields._
  def apply(coord: MessageCoordinate, timestamp: DateTime) =
    new MessageBuilder(coord.server, coord.user, coord.topic, coord.message, timestamp)

  implicit def messageBuilderDecodeJson(implicit dtd: DecodeJson[DateTime]): DecodeJson[MessageBuilder] =
    jdecode5L(MessageBuilder.apply)(server.toString, user.toString, topic.toString, id.toString, timestamp.toString)
}

