package chatless.ops.topic

import chatless.model.User
import chatless.model.topic.{MemberMode, Topic, TopicInit}
import chatless.ops.OperationFailure.precondition
import chatless.ops.OperationTypes._
import chatless.ops.Preconditions._
import chatless.ops._

trait CreateImpl { this: TopicOps with ImplUtils =>

  override def createTopic(caller: User, init: TopicInit): OperationResult[Created[Topic]] = for {
    /*-- make sure that we're not somehow trying to create a topic for a non-local user --*/
    _ <- precondition(caller.server == serverId.server, CREATE_TOPIC, USER_NOT_LOCAL, "user" -> caller.coordinate, "server" -> serverId)
    /*-- create the actual topic --*/
    topic <- topicDao.createLocal(caller.id, init) leftMap { DbOperationFailed(CREATE_TOPIC, caller.coordinate, _) }
    /*-- set caller as first member --*/
    member <- setMemberModeOp(SET_FIRST_MEMBER, topic.coordinate, caller.coordinate, MemberMode.creator)
  } yield Created(topic)

}
