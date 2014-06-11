package chatless.model

import org.joda.time.DateTime
import scalaz._
import argonaut._
import Argonaut._
import scalaz.syntax.id._
import chatless.model.topic.MemberMode
import ids._

case class MessageBuilder(
  server: String @@ ServerId,
  user: String @@ UserId,
  topic: String @@ TopicId,
  message: String @@ MessageId,
  timestamp: DateTime) {

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

  def invitation(sender: UserCoordinate, join: TopicCoordinate, mode: MemberMode, body: Json) =
    InvitationMessage(server, user, topic, message, timestamp, sender, join, mode, body)

  def invitedUser(sender: UserCoordinate, invitee: UserCoordinate, mode: MemberMode) =
    InvitedUserMessage(server, user, topic, message, timestamp, sender, invitee, mode)
}

object MessageBuilder {
  def at(coord: MessageCoordinate, timestamp: DateTime) =
    new MessageBuilder(coord.server, coord.user, coord.topic, coord.message, timestamp)

  def blank(tc: TopicCoordinate) = new MessageBuilder(tc.server, tc.user, tc.topic, "".messageId, DateTime.now())
}

