package chatless.models
import chatless._
import argonaut.{CodecJson, Json}
import org.joda.time.DateTime

case class MessageM(
    mid: MessageId,
    tid: TopicId,
    uid: UserId,
    timestamp: DateTime,
    body: Json)
  extends AccessModel


object MessageM {
  val MID = "mid"
  val TID = "tid"
  val UID = "uid"
  val BODY = "body"
  val TIMESTAMP = "timestamp"

  implicit def MessageMCodecJ: CodecJson[MessageM] =
    CodecJson.casecodec5(MessageM.apply, MessageM.unapply)(MID, TID, UID, TIMESTAMP, BODY)
}
