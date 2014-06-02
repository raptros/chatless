package chatless.db.mongo.codecs

import io.github.raptros.bson._
import Bson._
import argonaut._
import Argonaut._
import chatless.model._
import org.joda.time.DateTime

trait MessageCodecs { this: IdCodecs with CoordinateCodec with JsonCodec with TopicCodecs =>
  import Bson.stringDecodeField
  def postedMessageCodec = BsonMacros.deriveCaseCodecBson[PostedMessage]

  def bannerChangedCodec = BsonMacros.deriveCaseCodecBson[BannerChangedMessage]

  def userJoinedCodec = BsonMacros.deriveCaseCodecBson[UserJoinedMessage]

  def memberModeChangedCodec = BsonMacros.deriveCaseCodecBson[MemberModeChangedMessage]


  implicit def messageEncodeBson = EncodeBson[Message] {
    case m: PostedMessage => m.asBson(postedMessageCodec)
    case m: BannerChangedMessage => m.asBson(bannerChangedCodec)
    case m: UserJoinedMessage => m.asBson(userJoinedCodec)
    case m: MemberModeChangedMessage => m.asBson(memberModeChangedCodec)
  }

  implicit def messageDecodeBson: DecodeBson[Message] =
    postedMessageCodec ||| bannerChangedCodec ||| userJoinedCodec ||| memberModeChangedCodec

}
