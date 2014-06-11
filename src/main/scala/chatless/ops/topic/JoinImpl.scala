package chatless.ops.topic

import chatless.model.ids.MessageId
import chatless.model.{MessageBuilder, User}
import chatless.model.topic.{Member, MemberMode, Topic}
import scalaz.@@
import scalaz.syntax.id._
import chatless.ops._
import chatless.ops.OperationTypes._
import chatless.ops.DbOperationFailed

trait JoinImpl { this: TopicOps =>

  override def joinTopic(caller: User, topic: Topic): OperationResult[MemberMode] = for {
    existingMembership <- callerMembershipOp(JOIN_TOPIC, caller, topic)
    joinerMode = MemberMode.joinerMode(topic.mode)
    membership <- (existingMembership fold setModeOp(JOIN_TOPIC, caller, topic, joinerMode)) { _.mode.right }
    msg = MessageBuilder.blank(topic.coordinate).userJoined(caller.coordinate, membership)
    //todo: what really happens if this fails
    joinMsg <- messageDao.createNew(msg) leftMap { DbOperationFailed(SEND_MESSAGE, topic.coordinate, _) }
  } yield membership

  @inline
  protected def callerMembershipOp(op: OperationType, caller: User, topic: Topic): OperationResult[Option[Member]] =
    topicMemberDao.get(topic.coordinate, caller.coordinate) leftMap {
      DbOperationFailed(op, topic.coordinate, _)
    }

  def setModeOp(op: OperationType, caller: User, topic: Topic, mode: MemberMode): OperationResult[MemberMode] =
    topicMemberDao.set(topic.coordinate, caller.coordinate, mode).bimap(
      DbOperationFailed(op, topic.coordinate, _),
      _.mode
    )
}
