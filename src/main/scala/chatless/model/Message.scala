package chatless.model

import chatless._
import org.joda.time.DateTime
import argonaut._
import Argonaut._

sealed abstract class Message extends HasCoordinate[MessageCoordinate] {
  def server: String
  def topic: String
  def user: String
  def id: String
  def timestamp: DateTime

  lazy val coordinate = MessageCoordinate(server, topic, user, id)
}

case class PostedMessage(
    server: String,
    topic: String,
    user: String,
    id: String,
    timestamp: DateTime,
    poster: UserCoordinate,
    body: Json)
  extends Message

case class BannerChangedMessage(
    server: String,
    topic: String,
    user: String,
    id: String,
    timestamp: DateTime,
    poster: UserCoordinate,
    banner: String)
  extends Message

case class UserJoinedMessage(
    server: String,
    topic: String,
    user: String,
    id: String,
    timestamp: DateTime,
    joined: UserCoordinate)
  extends Message



object Message {
  implicit def messageEncodeJson(implicit dte: EncodeJson[DateTime]) = EncodeJson[Message] { m =>
    extend {
      ("server" := m.server) ->:
        ("topic" := m.topic) ->:
        ("user" := m.user) ->:
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
