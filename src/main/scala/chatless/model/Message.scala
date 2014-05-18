package chatless.model

import chatless._
import org.joda.time.DateTime
import argonaut._
import Argonaut._

import scala.language.experimental.macros

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

case class UserJoinedMessage(
    server: String,
    user: String,
    topic: String,
    id: String,
    timestamp: DateTime,
    joined: UserCoordinate)
  extends Message("jnd") {

  def modify(server: String, user: String, topic: String, id: String, timestamp: DateTime) =
    copy(server = server, user = user, topic = topic, id = id, timestamp = timestamp)
}



object Message {
  implicit def messageEncodeJson(implicit dte: EncodeJson[DateTime]) = EncodeJson[Message] { m =>
    extend {
      ("server" := m.server) ->:
        ("user" := m.user) ->:
        ("topic" := m.topic) ->:
        ("id" := m.id) ->:
        ("timestamp" := m.timestamp) ->:
        jEmptyObject
    } apply m
  }

  private def extend(json: Json): Message => Json = {
    case m: PostedMessage => ("poster" := m.poster) ->: ("body" := m.body) ->: json
    case m: BannerChangedMessage => ("poster" := m.poster) ->: ("banner" := m.banner) ->: json
    case m: UserJoinedMessage => ("joined" := m.joined) ->: json
  }

  implicit def messageDecodeJson(implicit dtd: DecodeJson[DateTime]): DecodeJson[Message] = for {
    mb <- MessageBuilder.messageBuilderDecodeJson
    f <- decodeBannerChanged ||| decodePostedMessage ||| decodeUserJoined
  } yield f(mb)

  private def posterDecode = jdecode1L(identity[UserCoordinate])("poster")

  private def decodePostedMessage = (posterDecode &&& jdecode1L(identity[Json])("body")) map {
    getMessage { _ postedT _ }
  }

  private def decodeBannerChanged = (posterDecode &&& jdecode1L(identity[String])("banner")) map {
    getMessage { _ bannerChangedT _ }
  }

  private def decodeUserJoined = jdecode1L(identity[UserCoordinate])("joined") map { getMessage { _ userJoined _} }

  private def getMessage[A](f: (MessageBuilder, A) => Message)(a:A)(mb: MessageBuilder): Message = f(mb, a)
}
