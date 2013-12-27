package chatless.model

import chatless._
import com.novus.salat.annotations._
import org.joda.time.DateTime

case class Event(
    kind: EventKind.Value,
    action: Action.Value,
    @Key("_id") id: Option[EventId] = None,
    @Ignore pos: Option[Long] = None,
    parent: Option[EventId] = None,
    timestamp: Option[DateTime] = None,
    uid: Option[UserId] = None,
    cid: Option[UserId] = None,
    tid: Option[TopicId] = None,
    rid: Option[RequestId] = None,
    mid: Option[MessageId] = None,
    field: Option[String] = None,
    value: ValueContainer) {

  @Persist val position = pos
}

object Event {
  val KIND      = "kind"
  val ACTION    = "action"
  val ID        = "id"
  val POSITION  = "position"
  val PARENT    = "parent"
  val TIMESTAMP = "timestamp"
  val UID       = "uid"
  val TID       = "tid"
  val CID       = "cid"
  val RID       = "rid"
  val MID       = "mid"
  val FIELD     = "field"
  val VALUE     = "value"

  def userUpdate[A](action: Action.Value, id: UserId, field: String, value: A)(implicit containable: ContainableValue[A]) =
    Event(
      kind = EventKind.USER_UPDATE,
      action = action,
      timestamp = Some(DateTime.now()),
      uid = Some(id),
      field = Some(field),
      value = containable.contain(value))

  def topicUpdate[A](action: Action.Value, cid: UserId, tid: TopicId, field: String, value: A)(implicit containable: ContainableValue[A]) =
    Event(
      kind = EventKind.TOPIC_UPDATE,
      action = action,
      cid = Some(cid),
      timestamp = Some(DateTime.now()),
      tid = Some(tid),
      field = Some(field),
      value = containable.contain(value))

  def topicCreate(topic: Topic) = Event(
    kind = EventKind.TOPIC_UPDATE,
    action = Action.CREATE,
    timestamp = Some(DateTime.now()),
    cid = Some(topic.op),
    tid = Some(topic.id),
    value = TopicVC(topic))

  def messageCreate(message: Message) = Event(
    kind = EventKind.MESSAGE,
    action = Action.CREATE,
    timestamp = Some(message.timestamp),
    cid = Some(message.uid),
    tid = Some(message.tid),
    mid = Some(message.id),
    value = JDocVC(message.body))
}

