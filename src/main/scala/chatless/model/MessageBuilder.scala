package chatless.model

import org.joda.time.DateTime
import argonaut._
import Argonaut._
import scalaz.syntax.id._

case class MessageBuilder(server: String, user: String, topic: String, message: String, timestamp: DateTime) {
  def posted(poster: UserCoordinate, body: Json) =
    PostedMessage(server, user, topic, message, timestamp, poster, body)

  val postedT = (posted _).tupled

  def bannerChanged(poster: UserCoordinate, banner: String) =
    BannerChangedMessage(server, user, topic, message, timestamp, poster, banner)

  val bannerChangedT = (bannerChanged _).tupled

  def userJoined(joined: UserCoordinate) =
    UserJoinedMessage(server, user, topic, message, timestamp, joined)
}

class ReverseMessageBuilder(tc: TopicCoordinate, build: MessageBuilder => Message) {
  def apply(id: String, timestamp: DateTime = DateTime.now()) = MessageBuilder.at(tc message id, timestamp) |> build
}

class RMBBuilder(tc: TopicCoordinate) {
  private def rmb(build: MessageBuilder => Message) = new ReverseMessageBuilder(tc, build)
  def posted(poster: UserCoordinate, body: Json) = rmb { _.posted(poster, body) }

  def bannerChanged(poster: UserCoordinate, banner: String) = rmb { _.bannerChanged(poster, banner) }

  def userJoined(joined: UserCoordinate) = rmb { _.userJoined(joined) }
}

object MessageBuilder {
  def at(coord: MessageCoordinate, timestamp: DateTime) =
    new MessageBuilder(coord.server, coord.user, coord.topic, coord.message, timestamp)

  implicit def messageBuilderDecodeJson(implicit dtd: DecodeJson[DateTime]): DecodeJson[MessageBuilder] =
    jdecode5L(MessageBuilder.apply)("server", "user", "topic", "id", "timestamp")
  
  def reverse(tc: TopicCoordinate) = new RMBBuilder(tc)

  val tupled = (MessageBuilder.apply _).tupled
}

