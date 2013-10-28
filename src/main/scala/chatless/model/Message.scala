package chatless.model

import chatless._
import org.joda.time.DateTime

case class Message(
    id: MessageId,
    tid: TopicId,
    uid: UserId,
    timestamp: DateTime,
    body: JDoc)

object Message {
  val MID = "mid"
  val TID = "tid"
  val UID = "uid"
  val BODY = "body"
  val TIMESTAMP = "timestamp"

  implicit val containableMessage = new ContainableValue[Message] {
    def contain(a: Message) = MessageVC(a)
  }
}
