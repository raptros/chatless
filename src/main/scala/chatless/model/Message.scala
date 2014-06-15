package chatless.model

import chatless._
import org.joda.time.DateTime
import argonaut._
import Argonaut._

import scala.language.experimental.macros
import chatless.macros.JsonMacros
import chatless.model.topic.MemberMode

import scalaz._
import chatless.model.ids._

sealed abstract class Message(shortName: String) extends HasCoordinate[MessageCoordinate] { self =>
  /** this type requirement means that change will always return the same subtype of Message that change was called on.
    * for instance, if you have a value `m` of type `A \<: Message`, you can safely call
    * {{{
    * m.change("whatever").asInstanceOf[A]
    * }}}
    */
  type M >: self.type <: Message { type M = self.M }

  def server: String @@ ServerId
  def user: String @@ UserId
  def topic: String @@ TopicId
  def id: String @@ MessageId
  def timestamp: DateTime

  lazy val coordinate = MessageCoordinate(server, user, topic, id)


  def change(part: String, timestamp: DateTime = DateTime.now()): M=
    modify(id = MessageId(s"$shortName-$part"), timestamp = timestamp)

  def modify(
    server: String @@ ServerId = server,
    user: String @@ UserId = user,
    topic: String @@ TopicId = topic,
    id: String @@ MessageId = id,
    timestamp: DateTime = timestamp): M
}

case class PostedMessage(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime,
    poster: UserCoordinate,
    body: Json)
  extends Message("pst") {

  type M = PostedMessage

  def modify(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime) = copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}

object PostedMessage {
  implicit def codecJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]) =
    JsonMacros.deriveCaseCodecJson[PostedMessage]
}

case class BannerChangedMessage(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime,
    poster: UserCoordinate,
    banner: String)
  extends Message("bnr") {

  type M = BannerChangedMessage

  def modify(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime) = copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}

object BannerChangedMessage {
  implicit def codecJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]) =
    JsonMacros.deriveCaseCodecJson[BannerChangedMessage]
}

case class UserJoinedMessage(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime,
    joined: UserCoordinate,
    mode: MemberMode)
  extends Message("jnd") {

  type M = UserJoinedMessage

  def modify(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime) = copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}

object UserJoinedMessage {
  implicit def codecJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]) =
    JsonMacros.deriveCaseCodecJson[UserJoinedMessage]
}

case class InvitationMessage(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime,
    sender: UserCoordinate,
    join: TopicCoordinate,
    mode: MemberMode,
    body: Json)
  extends Message("pls") {

  type M = InvitationMessage

  def modify(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime) = copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}

object InvitationMessage {
  implicit def codecJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]) =
    JsonMacros.deriveCaseCodecJson[InvitationMessage]
}

case class InvitedUserMessage(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime,
    sender: UserCoordinate,
    invitee: UserCoordinate,
    mode: MemberMode)
  extends Message("inv") {

  type M = InvitedUserMessage

  def modify(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime) = copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}

object InvitedUserMessage {
  implicit def codecJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]) =
    JsonMacros.deriveCaseCodecJson[InvitedUserMessage]
}

case class MemberModeChangedMessage(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime,
    member: UserCoordinate,
    changer: UserCoordinate,
    mode: MemberMode
  ) extends Message("mdc") {

  type M = MemberModeChangedMessage

  def modify(
    server: String @@ ServerId,
    user: String @@ UserId,
    topic: String @@ TopicId,
    id: String @@ MessageId,
    timestamp: DateTime) = copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}

object MemberModeChangedMessage {
  implicit def codecJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]) =
    JsonMacros.deriveCaseCodecJson[MemberModeChangedMessage]
}

object Message {

  implicit def messageDecodeJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]): DecodeJson[Message] =
    PostedMessage.codecJson orElse
      BannerChangedMessage.codecJson orElse
      UserJoinedMessage.codecJson orElse
      MemberModeChangedMessage.codecJson orElse
      InvitationMessage.codecJson orElse
      InvitedUserMessage.codecJson

  implicit def messageEncodeJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]): EncodeJson[Message] =
    EncodeJson {
      case m: PostedMessage => m.asJson
      case m: BannerChangedMessage => m.asJson
      case m: UserJoinedMessage => m.asJson
      case m: MemberModeChangedMessage => m.asJson
      case m: InvitedUserMessage => m.asJson
      case m: InvitationMessage => m.asJson
    }
}
