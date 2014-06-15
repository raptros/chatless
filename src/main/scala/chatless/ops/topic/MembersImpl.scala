package chatless.ops.topic

import chatless.model.{RMB, TopicCoordinate, UserCoordinate, User}
import chatless.model.topic.{PartialMember, Member, MemberMode, Topic}
import scalaz._
import scalaz.syntax.id._
import chatless.db.DbResult

import chatless.ops._
import OperationTypes._
import Preconditions._
import OperationFailure.{precondition, preconditionLift}

trait MembersImpl { this: TopicOps with ImplUtils =>

  override def getMembers(caller: User, topic: Topic) = getMembersInner(caller, topic).underlying

  def getMembersInner(caller: User, topic: Topic): ListT[OperationResult, PartialMember] = for {
    /*-- determine that the caller can read the topic --*/
    mode <- liftedCallerEffectiveMode[ListT](LIST_MEMBERS, caller, topic)
    _ <- preconditionLift[ListT](mode.read, LIST_MEMBERS, READ_DENIED, "topic" -> topic.coordinate)

    /*--- get the list of members --*/
    member <- listMembers(caller, topic)
  } yield member.partial


  protected def listMembers(caller: User, topic: Topic) = ListT[OperationResult, Member] {
    topicMemberDao.list(topic.coordinate) leftMap {
      DbOperationFailed(LIST_MEMBERS, topic.coordinate, _)
    }
  }
  override def getMember(caller: User, topic: Topic, member: UserCoordinate) = getMemberInner(caller, topic, member).run

  def getMemberInner(caller: User, topic: Topic, member: UserCoordinate): OptionT[OperationResult, MemberMode] = for {
    /*-- determine that the caller can read the topic --*/
    mode <- liftedCallerEffectiveMode[OptionT](GET_MEMBER, caller, topic)
    _ <- preconditionLift[OptionT](mode.read, GET_MEMBER, READ_DENIED, "topic" -> topic.coordinate, "member" -> member)

    /*--- get the target member --*/
    result <- getMemberOp(GET_MEMBER, topic.coordinate, member)
  } yield result.mode


  override def setMember(caller: User, topic: Topic, member: UserCoordinate, mode: MemberMode) = for {
    /*-- determine that the caller has permission to modify memberships --*/
    callerMode <- callerEffectiveMode(SET_MEMBER, caller, topic)
    _ <- precondition(callerMode.setMember, SET_MEMBER, SET_MEMBER_DENIED, "topic" -> topic.coordinate, "member" -> member)

    /*-- only permit modifying of users that are already members --*/
    current <- getMemberOp(SET_MEMBER, topic.coordinate, member).run
    _ <- precondition(current.nonEmpty, SET_MEMBER, USER_NOT_MEMBER, "topic" -> topic.coordinate, "user" -> member)

    /*-- perform the actual modification --*/
    newMode <- setMemberModeOp(SET_MEMBER, topic.coordinate, member, mode)

    /*-- tell the topic about the mode change --*/
    msg <- sendMessageOp(SEND_MESSAGE, topic.coordinate, RMB.memberModeChanged(member, caller.coordinate, newMode))
  } yield newMode
}
