package chatless.ops.topic

import chatless.model._
import chatless.model.topic.{MemberMode, Member, Topic}
import argonaut.Json
import chatless.ops._

import OperationTypes._
import Preconditions._
import OperationFailure.precondition
import chatless.ops.topic

trait InviteImpl { this: TopicOps with ImplUtils =>

  override def inviteUser(caller: User, topic: Topic, user: User, body: Json) = for {
    /*-- ensure the user can send invites --*/
    topicMembership <- callerEffectiveMode(SEND_INVITE, caller, topic)
    _ <- precondition(topicMembership.invite, SEND_INVITE, INVITE_DENIED, "topic" -> topic.coordinate)

    /*-- don't send an invite if the target user is already in the topic! --*/
    existing <- getMemberOp(SEND_INVITE, topic.coordinate, user.coordinate).run
    _ <- precondition(existing.isEmpty, SEND_INVITE, USER_ALREADY_MEMBER, "topic" -> topic.coordinate, "user" -> user.coordinate)

    /*-- find out where to send invitations, and if the user can send invitations there --*/
    invitesTopic <- getTopicOp(SEND_INVITE, user.coordinate.topic(user.invites))
    invitesTopicMembership <- joinTopic(caller, invitesTopic)
    _ <- precondition(invitesTopicMembership.write, SEND_INVITE, WRITE_DENIED, "topic" -> topic.coordinate)

    /*-- create a membership for the invitee --*/
    memberMode <- setMemberModeOp(ADD_MEMBER, topic.coordinate, user.coordinate, MemberMode.invitedMode(topic.mode))

    /*-- tell the user about the invitation --*/
    invite = RMB.invitation(caller.coordinate, topic.coordinate, memberMode, body)
    invMsg <- sendMessageOp(SEND_INVITE, invitesTopic.coordinate, invite)

    /*-- tell the topic that a user has been invited --*/
    rmbNotification = RMB.invitedUser(caller.coordinate, user.coordinate, memberMode)
    notification <- sendMessageOp(SEND_MESSAGE, topic.coordinate, rmbNotification)
  } yield notification

  def getTopicOp(op: OperationType, tc: TopicCoordinate): OperationResult[Topic] =
    topicDao.get(tc) leftMap { DbOperationFailed(op, tc.parent, _) }
}
