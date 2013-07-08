package chatless.db

import chatless.{UserId, TopicId, MessageId}

sealed abstract class StateError(msg:String) extends Throwable(msg)

case class UnhandleableMessage(what:Any) extends StateError("can't handle ${what.getClass}.")

sealed abstract class TopicRetrievalError(tid:TopicId, cid:UserId, whyNot:String)
  extends StateError(s"""could not get topic "$tid" for caller "$cid": $whyNot """)

case class TopicNotFoundError(tid:TopicId, cid:UserId) extends TopicRetrievalError(tid, cid, "not found")

case class OperationNotSupported(cid:UserId, res:OpRes, spec:OpSpec)
  extends StateError(s"cannot perform operation $spec on resource $res for caller $cid")


