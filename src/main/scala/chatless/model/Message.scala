package chatless.model

import chatless._
import com.novus.salat.annotations._
import org.joda.time.DateTime

case class Message(
    @Key("_id") id: MessageId,
    tid: TopicId,
    uid: UserId,
    timestamp: DateTime,
    body: JDoc,
    @Ignore pos: Option[Long] = None) {

  @Persist val position = pos
}

object Message {
  val MID = "mid"
  val TID = "tid"
  val UID = "uid"
  val BODY = "body"
  val TIMESTAMP = "timestamp"
  val POSITION = "position"

  implicit val containableMessage = new ContainableValue[Message] {
    def contain(a: Message) = MessageVC(a)
  }
}
