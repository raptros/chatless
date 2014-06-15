package chatless.ops.topic

import chatless.model.ids.MessageId
import chatless.model.{RMB, UserJoinedMessage, MessageBuilder, User}
import chatless.model.topic.{Member, MemberMode, Topic}
import scalaz.{Monad, OptionT, @@}
import scalaz.syntax.id._
import scalaz.syntax.bind._
import scalaz.syntax.std.option._
import chatless.ops._
import chatless.ops.OperationTypes._
import chatless.ops.DbOperationFailed

trait JoinImpl { this: TopicOps with ImplUtils =>

  override def joinTopic(caller: User, topic: Topic) =
    getMemberModeOp(JOIN_TOPIC, topic.coordinate, caller.coordinate) alternatively performJoin(caller, topic)

  def performJoin(caller: User, topic: Topic): OperationResult[MemberMode] = for {
    newMember <- setMemberModeOp(JOIN_TOPIC, topic.coordinate, caller.coordinate, MemberMode.joinerMode(topic.mode))
    //todo: what really happens if this fails
    joinMsg <- sendMessageOp(SEND_MESSAGE, topic.coordinate, RMB.userJoined(caller.coordinate, newMember))
  } yield newMember

}
