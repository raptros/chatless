package chatless.model

import chatless._
import com.novus.salat.annotations._
import org.joda.time.DateTime


case class Event(
    kind: EventKind.Value,
    action: Action.Value,
    @Key("_id") id: Option[EventId] = None,
    parent: Option[EventId] = None,
    timestamp: Option[DateTime] = None,
    uid: Option[UserId] = None,
    cid: Option[UserId] = None,
    tid: Option[TopicId] = None,
    rid: Option[RequestId] = None,
    field: Option[String] = None,
    value: ValueContainer)

object Event {

  def userUpdate[A](action: Action.Value, id: UserId, field: String, value: A)(implicit containable: ContainableValue[A]) =
    Event(
      kind = EventKind.USER_UPDATE,
      action = action,
      uid = Some(id),
      field = Some(field),
      value = containable.contain(value))

  def topicUpdate[A](action: Action.Value, cid: UserId, tid: TopicId, field: String, value: A)(implicit containable: ContainableValue[A]) =
    Event(
      kind = EventKind.TOPIC_UPDATE,
      action = action,
      cid = Some(cid),
      tid = Some(tid),
      field = Some(field),
      value = containable.contain(value))

  def topicCreate(topic: Topic) = Event(
    kind = EventKind.TOPIC_UPDATE,
    action = Action.CREATE,
    cid = Some(topic.op),
    tid = Some(topic.id),
    value = TopicVC(topic))
}
