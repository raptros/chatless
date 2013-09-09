package chatless.events.model

import chatless._
import org.json4s._

case class Delta(
  res: Resource.Value,
  action: Action.Value,
  field: Option[String] = None,
  cid: Option[UserId] = None,
  uid: Option[UserId] = None,
  tid: Option[TopicId] = None,
  mid: Option[MessageId] = None,
  rid: Option[RequestId] = None,
  newVal: Option[JValue] = None)
