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
