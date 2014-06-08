package chatless.ops.topic

import chatless.model.ServerCoordinate
import chatless.db._
import com.google.inject.Inject
import chatless.wiring.params.ServerIdParam

class TopicOpsImpl @Inject() (
    @ServerIdParam
    val serverId: ServerCoordinate,
    val userDao: UserDAO,
    val topicDao: TopicDAO,
    val messageDao: MessageDAO,
    val topicMemberDao: TopicMemberDAO)
  extends TopicOps
  with InviteImpl
  with CreateImpl
  with JoinImpl
  with SendMessageImpl
  with MembersImpl {
}

