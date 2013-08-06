package chatless.op2
import chatless._

sealed abstract class Resource

case class ResUser(uid:UserId) extends Resource

case class ResTopic(tid:TopicId) extends Resource

case class ResMessages(tid:TopicId) extends Resource