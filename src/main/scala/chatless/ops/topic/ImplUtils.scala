package chatless.ops.topic

import chatless.model._
import chatless.model.topic.{Member, MemberMode, Topic}
import chatless.ops._
import OperationTypes.OperationType
import chatless.ops.topic

import scalaz._
import scalaz.syntax.id._

trait ImplUtils { this: TopicOps =>

  def getMemberOp(op: OperationType, tc: TopicCoordinate, member: UserCoordinate) =
    OptionT[OperationResult, Member] {
      topicMemberDao.get(tc, member) leftMap { DbOperationFailed(op, tc, _) }
    }

  def getMemberModeOp(op: OperationType, tc: TopicCoordinate, member: UserCoordinate): OptionT[OperationResult, MemberMode] =
    getMemberOp(op, tc, member) map { _.mode }

  import scala.language.higherKinds
  def liftedCallerEffectiveMode[MT[_[+_], _] : MonadTrans](op: OperationType, caller: User, topic: Topic) = liftMOR {
    callerEffectiveMode(op, caller, topic)
  }

  def callerEffectiveMode(op: OperationType, caller: User, topic: Topic): OperationResult[MemberMode] =
    if (caller.server == topic.server && caller.id == topic.user)
      MemberMode.creator.right //the creator of a topic always has this mode, no matter what's in the DB
    else
      getMemberOp(op, topic.coordinate, caller.coordinate).fold(_.mode, MemberMode.nonMemberMode(topic.mode))

  def setMemberModeOp(op: OperationType, tc: TopicCoordinate, member: UserCoordinate, mode: MemberMode): OperationResult[MemberMode] =
    topicMemberDao.set(tc, member, mode).bimap(DbOperationFailed(op, tc, _), _.mode)

  def sendMessageOp[A <: Message](op: OperationType, tc: TopicCoordinate, rmb: RMB[A]): OperationResult[Created[A]] =
    messageDao.createNew(rmb.blank(tc)).bimap(DbOperationFailed(op, tc, _), Created.apply)
}
