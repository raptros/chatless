package chatless.ops.topic

import chatless.model.User
import chatless.model.topic.{MemberMode, Topic}
import chatless.ops._
import OperationTypes.OperationType

import scalaz._
import scalaz.syntax.id._

trait ImplUtils { this: TopicOps =>

  import scala.language.higherKinds
  def liftedCallerEffectiveMode[MT[_[+_], _] : MonadTrans](op: OperationType, caller: User, topic: Topic) = liftMOR {
    callerEffectiveMode(op, caller, topic)
  }

  def callerEffectiveMode(op: OperationType, caller: User, topic: Topic): OperationResult[MemberMode] =
    if (caller.server == topic.server && caller.id == topic.user)
      MemberMode.creator.right //the creator of a topic always has this mode, no matter what's in the DB
    else
      topicMemberDao.get(topic.coordinate, caller.coordinate).bimap(
        DbOperationFailed(op, topic.coordinate, _),
        _.fold(MemberMode.nonMemberMode(topic.mode)) { _.mode }
      )

}
