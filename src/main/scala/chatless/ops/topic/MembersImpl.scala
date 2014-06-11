package chatless.ops.topic

import chatless.model.{UserCoordinate, User}
import chatless.model.topic.{PartialMember, Member, MemberMode, Topic}
import scalaz._
import scalaz.syntax.id._
import chatless.db.DbResult

import chatless.ops._
import OperationTypes._
import Preconditions._
import OperationFailure.BooleanFailureConditions

trait MembersImpl { this: TopicOps with ImplUtils =>

  override def getMembers(caller: User, topic: Topic) = getMembersInner(caller, topic).underlying

  def getMembersInner(caller: User, topic: Topic): ListT[OperationResult, PartialMember] = for {
    effectiveMode <- liftedCallerEffectiveMode[ListT](LIST_MEMBERS, caller, topic)
    _ <- effectiveMode.read.failUnlessLiftM[ListT] {
      PreconditionFailed(LIST_MEMBERS, READ_DENIED, "topic" -> topic.coordinate)
    }
    member <- listMembers(caller, topic)
  } yield member.partial


  protected def listMembers(caller: User, topic: Topic) = ListT[OperationResult, Member] {
    topicMemberDao.list(topic.coordinate) leftMap {
      DbOperationFailed(LIST_MEMBERS, topic.coordinate, _)
    }
  }
  override def getMember(caller: User, topic: Topic, member: UserCoordinate) = getMemberInner(caller, topic, member).run

  def getMemberInner(caller: User, topic: Topic, member: UserCoordinate): OptionT[OperationResult, MemberMode] = for {
    effectiveMode <- liftedCallerEffectiveMode[OptionT](GET_MEMBER, caller, topic)
    _ <- effectiveMode.read.failUnlessLiftM[OptionT] {
      PreconditionFailed(GET_MEMBER, READ_DENIED, "topic" -> topic.coordinate, "member" -> member)
    }
    result <- OptionT[OperationResult, Member] {
      topicMemberDao.get(topic.coordinate, member) leftMap {
        DbOperationFailed(GET_MEMBER, topic.coordinate, _)
      }
    }
  } yield result.mode


  override def setMember(caller: User, topic: Topic, member: UserCoordinate, mode: MemberMode) = for {
    effectiveMode <- callerEffectiveMode(SET_MEMBER, caller, topic)
    _ <- effectiveMode.setMember failUnlessM {
      PreconditionFailed(SET_MEMBER, SET_MEMBER_DENIED, "topic" -> topic.coordinate, "member" -> member)
    }
    modified <- topicMemberDao.set(topic.coordinate, member, mode) leftMap {
      DbOperationFailed(SET_MEMBER, topic.coordinate, _)
    }
  } yield modified.mode
}
