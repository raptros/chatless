package chatless.ops.topic

import chatless.model.topic.{MemberMode, Topic}
import chatless.model.{RMB, User}
import chatless.ops.OperationTypes._
import chatless.ops._

trait JoinImpl { this: TopicOps with ImplUtils =>

  override def joinTopic(caller: User, topic: Topic) =
    getMemberModeOp(JOIN_TOPIC, topic.coordinate, caller.coordinate) alternatively performJoin(caller, topic)

  def performJoin(caller: User, topic: Topic): OperationResult[MemberMode] = for {
    newMember <- setMemberModeOp(JOIN_TOPIC, topic.coordinate, caller.coordinate, MemberMode.joinerMode(topic.mode))
    //todo: what really happens if this fails
    joinMsg <- sendMessageOp(SEND_MESSAGE, topic.coordinate, RMB.userJoined(caller.coordinate, newMember))
  } yield newMember

}
