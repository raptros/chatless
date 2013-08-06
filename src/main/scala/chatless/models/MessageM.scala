package chatless.models
import chatless._
import argonaut.{CodecJson, Json}

case class MessageM(
    mid: MessageId,
    tid: TopicId,
    uid: UserId,
    body: Json)
  extends AccessModel


object MessageM {
  val MID = "mid"
  val TID = "tid"
  val UID = "uid"
  val BODY = "body"

  implicit def MessageMCodecJ: CodecJson[MessageM] =
    CodecJson.casecodec4(MessageM.apply, MessageM.unapply)(MID, TID, UID, BODY)
}
