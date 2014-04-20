package chatless.model

import chatless._
import org.joda.time.DateTime

case class Message(
    id: MessageId,
    tid: TopicId,
    uid: UserId,
    timestamp: DateTime,
    body: JDoc,
    pos: Option[Long] = None) {

}

object Message {
  val ID = "id"
  val TID = "tid"
  val UID = "uid"
  val BODY = "body"
  val TIMESTAMP = "timestamp"
  val POSITION = "position"

}
