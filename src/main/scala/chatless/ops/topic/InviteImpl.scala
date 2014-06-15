package chatless.ops.topic

import chatless.model.{RMB, Message, MessageBuilder, User}
import chatless.model.topic.{MemberMode, Member, Topic}
import argonaut.Json
import chatless.ops._

import OperationTypes._
import Preconditions._
import OperationFailure.BooleanFailureConditions

trait InviteImpl { this: TopicOps with ImplUtils =>

  override def inviteUser(caller: User, topic: Topic, user: User, body: Json) = for {
    /*-- ensure the user can send invites --*/
    topicMembership <- callerEffectiveMode(SEND_INVITE, caller, topic)
    _ <- topicMembership.invite failUnlessM PreconditionFailed(SEND_INVITE, INVITE_DENIED, "topic" -> topic.coordinate)
    /*-- don't send an invite if the target user is already in the topic! --*/
    targetMembership <- topicMemberDao.get(topic.coordinate, user.coordinate) leftMap {
      DbOperationFailed(SEND_INVITE, topic.coordinate, _)
    }
    _ <- targetMembership.nonEmpty failWhenM {
      PreconditionFailed(SEND_INVITE, USER_ALREADY_MEMBER, "topic" -> topic.coordinate, "user" -> user.coordinate)
    }
    /*-- find out where to send invitations, and if the user can send invitations there --*/
    invitesTopic <- topicDao.get(user.coordinate.topic(user.invites)) leftMap {
      DbOperationFailed(SEND_INVITE, user.coordinate, _)
    }
    invitesTopicMembership <- joinTopic(caller, invitesTopic)
    _ <- invitesTopicMembership.write failUnlessM {
      PreconditionFailed(SEND_INVITE, WRITE_DENIED, "topic" -> topic.coordinate)
    }
    /*-- create a membership for the invitee --*/
    member <- topicMemberDao.set(topic.coordinate, user.coordinate, MemberMode.invitedMode(topic.mode)) leftMap {
      DbOperationFailed(ADD_MEMBER, topic.coordinate, _)
    }
    /*-- tell the user about the invitation --*/
    invite = RMB.invitation(caller.coordinate, topic.coordinate, member.mode, body)
    invMsg <- sendMessageOp(SEND_INVITE, invitesTopic.coordinate, invite)
    /*-- tell the topic that a user has been invited --*/
    rmbNotification = RMB.invitedUser(caller.coordinate, user.coordinate, member.mode)
    notification <- sendMessageOp(SEND_MESSAGE, topic.coordinate, rmbNotification)
  } yield notification
}
