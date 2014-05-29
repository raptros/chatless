package chatless.model

import org.joda.time.DateTime
import argonaut._
import Argonaut._
import scalaz.syntax.id._
import chatless.model.topic.MemberMode

case class MessageBuilder(server: String, user: String, topic: String, message: String, timestamp: DateTime) {
  def posted(poster: UserCoordinate, body: Json) =
    PostedMessage(server, user, topic, message, timestamp, poster, body)

  val postedT = (posted _).tupled

  def bannerChanged(poster: UserCoordinate, banner: String) =
    BannerChangedMessage(server, user, topic, message, timestamp, poster, banner)

  val bannerChangedT = (bannerChanged _).tupled

  def userJoined(joined: UserCoordinate, mode: MemberMode) =
    UserJoinedMessage(server, user, topic, message, timestamp, joined, mode)

  def memberModeChanged(member: UserCoordinate, changer: UserCoordinate, mode: MemberMode) =
    MemberModeChangedMessage(server, user, topic, message, timestamp, member, changer, mode)
}

object MessageBuilder {
  def at(coord: MessageCoordinate, timestamp: DateTime) =
    new MessageBuilder(coord.server, coord.user, coord.topic, coord.message, timestamp)

  def blank(tc: TopicCoordinate) = new MessageBuilder(tc.server, tc.user, tc.topic, "", DateTime.now())
}

