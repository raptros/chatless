package chatless.db.mongo.codecs

object Codecs
extends IdCodecs
with CoordinateCodec
with TopicCodecs
with JsonCodec
with MessageCodecs
