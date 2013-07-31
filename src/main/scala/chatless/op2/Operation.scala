package chatless.op2
import chatless._

sealed trait Operation


case class GetUser(uid:UserId) extends Operation

case class GetTopic(tid:TopicId) extends Operation


case class UpdateUser(uid:UserId, spec:UpdateSpec[_]) extends Operation

case class UpdateTopic(tid:TopicId, spec:UpdateSpec[_]) extends Operation

