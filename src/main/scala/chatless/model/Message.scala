package chatless.model

import chatless._
import org.joda.time.DateTime
import argonaut._
import Argonaut._

import scala.language.experimental.macros
import chatless.macros.JsonMacros
import chatless.model.topic.MemberMode

sealed abstract class Message(shortName: String) extends HasCoordinate[MessageCoordinate] {
  def server: String
  def user: String
  def topic: String
  def id: String
  def timestamp: DateTime

  lazy val coordinate = MessageCoordinate(server, user, topic, id)

  def change(part: String, timestamp: DateTime = DateTime.now()) =
    modify(id = s"$shortName-$part", timestamp = timestamp)

  def modify(
    server: String = server,
    user: String = user,
    topic: String = topic,
    id: String = id,
    timestamp: DateTime = timestamp): Message
}

case class PostedMessage(
    server: String,
    user: String,
    topic: String,
    id: String,
    timestamp: DateTime,
    poster: UserCoordinate,
    body: Json)
  extends Message("pst") {

  def modify(server: String, user: String, topic: String, id: String, timestamp: DateTime) =
    copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}

object PostedMessage {
  implicit def codecJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]) =
    JsonMacros.deriveCaseCodecJson[PostedMessage]
}

case class BannerChangedMessage(
    server: String,
    user: String,
    topic: String,
    id: String,
    timestamp: DateTime,
    poster: UserCoordinate,
    banner: String)
  extends Message("bnr") {

  def modify(server: String, user: String, topic: String, id: String, timestamp: DateTime) =
    copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}

object BannerChangedMessage {
  implicit def codecJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]) =
    JsonMacros.deriveCaseCodecJson[BannerChangedMessage]
}

case class UserJoinedMessage(
    server: String,
    user: String,
    topic: String,
    id: String,
    timestamp: DateTime,
    joined: UserCoordinate,
    mode: MemberMode)
  extends Message("jnd") {

  def modify(server: String, user: String, topic: String, id: String, timestamp: DateTime) =
    copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}

object UserJoinedMessage {
  implicit def codecJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]) =
    JsonMacros.deriveCaseCodecJson[UserJoinedMessage]
}

case class MemberModeChangedMessage(
    server: String,
    user: String,
    topic: String,
    id: String,
    timestamp: DateTime,
    member: UserCoordinate,
    changer: UserCoordinate,
    mode: MemberMode
  ) extends Message("mdc") {

  def modify(server: String, user: String, topic: String, id: String, timestamp: DateTime) =
    copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
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
      MemberModeChangedMessage.codecJson

  implicit def messageEncodeJson(implicit dte: EncodeJson[DateTime], dtd: DecodeJson[DateTime]): EncodeJson[Message] =
    EncodeJson {
      case m: PostedMessage => m.asJson
      case m: BannerChangedMessage => m.asJson
      case m: UserJoinedMessage => m.asJson
      case m: MemberModeChangedMessage => m.asJson
    }
}
