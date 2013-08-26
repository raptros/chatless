package chatless.models

import chatless._
import argonaut.{CodecJson, Json}

case class TopicM(
    tid: TopicId,
    title: String,
    public: Boolean,
    info: Json,
    op: UserId,
    sops: Set[UserId],
    participating: Set[UserId],
    tags: Set[String])
  extends AccessModel

object TopicM {
  val TID = TypedField[TopicId]("name")
  val TITLE = TypedField[String]("title")
  val PUBLIC = TypedField[Boolean]("public")
  val INFO = TypedField[Json]("info")
  val OP = TypedField[UserId]("op")
  val SOPS = TypedField[Set[UserId]]("sops")
  val PARTICIPATING = TypedField[Set[UserId]]("participating")
  val TAGS = TypedField[Set[String]]("tags")

  implicit def TopicMCodecJ: CodecJson[TopicM] =
    CodecJson.casecodec8(TopicM.apply, TopicM.unapply)(
      TID.name,
      TITLE.name,
      PUBLIC.name,
      INFO.name,
      OP.name,
      SOPS.name,
      PARTICIPATING.name,
      TAGS.name)
}
