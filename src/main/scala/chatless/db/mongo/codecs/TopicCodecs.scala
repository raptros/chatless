package chatless.db.mongo.codecs

import io.github.raptros.bson._
import Bson._
import argonaut._
import chatless.model.topic._
import chatless.model.TopicCoordinate

trait TopicCodecs { this: CoordinateCodec with JsonCodec =>
  implicit val topicModeCodecBson: CodecBson[TopicMode] = BsonMacros.deriveCaseCodecBson[TopicMode]
//    bsonCaseCodec3(TopicMode.apply, TopicMode.unapply)("muted", "open", "public")

  implicit def topicCodecBson: CodecBson[Topic] = BsonMacros.deriveCaseCodecBson[Topic]
//    bsonCaseCodec6(Topic.apply, Topic.unapply)("server", "user", "id", "banner", "info", "mode")
}
