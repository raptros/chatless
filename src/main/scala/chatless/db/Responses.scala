package chatless.db

import chatless.{UserId, TopicId, MessageId}
import argonaut._
import Argonaut._
import chatless.op2.{Operation, Specifier}

sealed abstract class StateError(msg: String) extends Throwable(msg) {
  def asJson: Json = ("msg" := msg) ->: jEmptyObject
}

case class UnhandleableMessage(what: Any) extends StateError("can't handle ${what.getClass}.")

sealed abstract class UserRetrievalError(uid: UserId, cid: UserId, whyNot: String)
  extends StateError(s"""could not get user "$uid" for caller "$cid": $whyNot""")

case class UserNotFoundError(uid: UserId, cid: UserId) extends UserRetrievalError(uid, cid, "not found")

sealed abstract class TopicRetrievalError(tid: TopicId, cid: UserId, whyNot: String)
  extends StateError(s"""could not get topic "$tid" for caller "$cid": $whyNot """)


case class TopicNotFoundError(tid: TopicId, cid: UserId) extends TopicRetrievalError(tid, cid, "not found")


case class OperationNotSupported(op: Operation)
  extends StateError(s"cannot perform operation $op")

/*
case class NonExistentField(field: String, res: Resource)
  extends StateError(s"cannot get field $field for $res")
*/

/*
case class AccessNotPermitted(cid: UserId, res: Resource, spec: Specifier)
  extends StateError(s"caller $cid is not allowed to do $spec on resource $res")
*/

