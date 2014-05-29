package chatless.db.mongo.codecs

import io.github.raptros.bson._
import Bson._
import argonaut._
import chatless.model.topic._
import chatless.model.TopicCoordinate

trait TopicCodecs { this: CoordinateCodec with JsonCodec =>
  implicit val memberModeCodecBson = BsonMacros.deriveCaseCodecBson[MemberMode]

  implicit val memberCodecBson = BsonMacros.deriveCaseCodecBson[Member]

  implicit val topicModeCodecBson: CodecBson[TopicMode] = BsonMacros.deriveCaseCodecBson[TopicMode]

  /**this must be a def or a lazy val, otherwise it NPEs when trying to encode Json */
  implicit lazy val topicCodecBson: CodecBson[Topic] = BsonMacros.deriveCaseCodecBson[Topic]
}
