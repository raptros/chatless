package chatless.model

import argonaut._
import chatless.model.topic.MemberMode
import ids._
import org.joda.time.DateTime
import scalaz._

trait RMB[A <: Message] {
  def mk(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    message: String @@ MessageId,
    timestamp: DateTime): A

  def at(mc: MessageCoordinate, timestamp: DateTime = DateTime.now()) =
    mk(mc.server, mc.user, mc.topic, mc.message, timestamp)

  def blank(tc: TopicCoordinate) = mk(tc.server, tc.user, tc.topic, "".messageId, DateTime.now())
}

object RMB {
  type Sig[A <: Message] = (String @@ ServerId, String @@ UserId, String @@ TopicId, String @@ MessageId, DateTime) => A

  def apply[A <: Message](f: Sig[A]): RMB[A] = new RMB[A] {
    override def mk(
      server: String @@ ServerId,
      user: String @@ UserId,
      topic: String @@ TopicId,
      message: String @@ MessageId,
      timestamp: DateTime) = f(server, user, topic, message, timestamp)
  }

  def posted(poster: UserCoordinate, body: Json) = apply {
    PostedMessage(_, _, _, _, _, poster, body)
  }

  def bannerChanged(poster: UserCoordinate, banner: String) = apply {
    BannerChangedMessage(_, _, _, _, _, poster, banner)
  }

  def userJoined(joined: UserCoordinate, mode: MemberMode) = apply {
    UserJoinedMessage(_, _, _, _, _, joined, mode)
  }

  def memberModeChanged(member: UserCoordinate, changer: UserCoordinate, mode: MemberMode) = apply {
    MemberModeChangedMessage(_, _, _, _, _, member, changer, mode)
  }

  def invitation(sender: UserCoordinate, join: TopicCoordinate, mode: MemberMode, body: Json) = apply {
    InvitationMessage(_, _, _, _, _, sender, join, mode, body)
  }

  def invitedUser(sender: UserCoordinate, invitee: UserCoordinate, mode: MemberMode) = apply {
    InvitedUserMessage(_, _, _, _, _, sender, invitee, mode)
  }
}
