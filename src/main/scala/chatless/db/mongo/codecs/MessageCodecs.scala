package chatless.db.mongo.codecs

import io.github.raptros.bson._
import Bson._
import argonaut._
import Argonaut._
import chatless.model._
import org.joda.time.DateTime

trait MessageCodecs { this: CoordinateCodec with JsonCodec =>

  def messageBuilderEncodeBson = BsonMacros.deriveCaseEncodeBson[MessageBuilder]

  implicit def messageEncodeBson: EncodeBson[Message] =
    EncodeBson[Message] { msg =>
      val b = DBO(
        "server" :> msg.server,
        "user" :> msg.user,
        "topic" :> msg.topic,
        "id" :> msg.id,
        "timestamp" :> msg.timestamp
      )
      msg match {
        case message: PostedMessage => b +@+ ("poster" :> message.poster) +@+ ("body" :> message.body)
        case message: BannerChangedMessage => b +@+ ("poster" :> message.poster) +@+ ("banner" :> message.banner)
        case message: UserJoinedMessage => b +@+ ("joined" :> message.joined)
      }
    }

  def messageBuilderDecodeBson =
    bdecodeTuple5[String, String, String, String, DateTime]("server", "user", "topic", "id", "timestamp") map {
      MessageBuilder.tupled
    }

  def getM[A](f: (MessageBuilder, A) => Message)(a: A)(mb: MessageBuilder) = f(mb, a)

  def postedMessageDecoding = bdecodeTuple2[UserCoordinate, Json]("poster", "body") map {
    getM { _ postedT _ }
  }

  def bannerChangedDecoding = bdecodeTuple2[UserCoordinate, String]("poster", "banner") map {
    getM { _ bannerChangedT _ }
  }

  def userJoinedDecoding = DecodeBson[MessageBuilder => Message] { dbo =>
    dbo.field[UserCoordinate]("joined") map { getM { _ userJoined _} }
  }

  implicit def messageDecodeBson: DecodeBson[Message] = for {
    mb <- messageBuilderDecodeBson
    f <- bannerChangedDecoding ||| postedMessageDecoding ||| userJoinedDecoding
  } yield f(mb)


}
