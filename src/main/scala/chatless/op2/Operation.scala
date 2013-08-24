package chatless.op2
import chatless._

//case class Operation(cid:UserId, res:Resource, spec:Specifier)

sealed trait Operation {
  type ASpec <: Specifier
  val cid: UserId

  val spec: ASpec
}

case class UserOp(cid: UserId, uid: UserId, spec: Specifier with ForUsers) extends Operation {
  type ASpec = Specifier with ForUsers
}

case class TopicOp(cid: UserId, tid: TopicId, spec: Specifier with ForTopics) extends Operation {
  type ASpec = Specifier with ForTopics
}

case class MessagesOp(cid: UserId, tid: TopicId, spec: Specifier with ForMessages) extends Operation {
  type ASpec = Specifier with ForMessages
}

case class EventOp(cid: UserId, spec: Specifier with ForEvents) extends Operation {
  type ASpec = Specifier with ForEvents
}