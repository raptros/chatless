package chatless.model

import chatless._
import org.joda.time.DateTime

case class Message(
    mid: MessageId,
    tid: TopicId,
    uid: UserId,
    timestamp: DateTime,
    body: Map[String, Any])
  extends BaseModel

object Message {
  val MID = "mid"
  val TID = "tid"
  val UID = "uid"
  val BODY = "body"
  val TIMESTAMP = "timestamp"
}
