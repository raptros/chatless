package chatless.ops.topic

import chatless.model.User
import chatless.model.topic.{MemberMode, Member, Topic}
import argonaut.Json
import chatless.ops._

import OperationTypes._
import Preconditions._
import OperationFailure.BooleanFailureConditions

trait InviteImpl { this: TopicOps =>

  override def inviteUser(caller: User, topic: Topic, user: User, body: Json): OperationResult[Member] = for {
    invitesTopic <- topicDao.get(user.coordinate.topic(user.invites)) leftMap {
      DbOperationFailed(SEND_INVITE, user.coordinate, _)
    }
    invitesTopicMembership <- joinTopic(caller, invitesTopic)
    _ <- invitesTopicMembership.write failUnlessM {
      PreconditionFailed(SEND_INVITE, WRITE_DENIED, "topic" -> topic.coordinate)
    }
    member <- topicMemberDao.set(topic.coordinate, user.coordinate, MemberMode.invitedMode(topic.mode)) leftMap {
      DbOperationFailed(ADD_MEMBER, topic.coordinate, _)
    }
  } yield member



}
