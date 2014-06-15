package chatless.ops.topic

import chatless.model.User
import chatless.model.topic.{MemberMode, Topic, TopicInit}
import chatless.ops._
import chatless.ops.OperationTypes._
import chatless.ops.Preconditions._
import OperationFailure.BooleanFailureConditions

trait CreateImpl { this: TopicOps with ImplUtils =>

  override def createTopic(caller: User, init: TopicInit): OperationResult[Created[Topic]] = for {
    _ <- (caller.server == serverId.server) failUnlessM {
      PreconditionFailed(CREATE_TOPIC, USER_NOT_LOCAL, "user" -> caller.coordinate, "server" -> serverId)
    }
    topic <- topicDao.createLocal(caller.id, init) leftMap {
      DbOperationFailed(CREATE_TOPIC, caller.coordinate, _)
    }
    member <- setMemberModeOp(SET_FIRST_MEMBER, topic.coordinate, caller.coordinate, MemberMode.creator)
  } yield Created(topic)

}
