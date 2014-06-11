package chatless.ops.topic
import chatless.ops._
import chatless.model._
import chatless.model.topic._
import chatless.db._
import argonaut._

trait TopicOps {
  val serverId: ServerCoordinate
  val userDao: UserDAO
  val topicDao: TopicDAO
  val messageDao: MessageDAO
  val topicMemberDao: TopicMemberDAO

  def createTopic(caller: User, init: TopicInit): OperationResult[Created[Topic]]

  def inviteUser(caller: User, topic: Topic, user: User, body: Json): OperationResult[Created[InvitedUserMessage]]

  def getMembers(caller: User, topic: Topic): OperationResult[List[PartialMember]]

  def getMember(caller: User, topic: Topic, member: UserCoordinate): OperationResult[Option[MemberMode]]

  def setMember(caller: User, topic: Topic, member: UserCoordinate, mode: MemberMode): OperationResult[MemberMode]

  def joinTopic(caller: User, topic: Topic): OperationResult[MemberMode]

  def sendMessage(caller: User, topic: Topic, message: Message): OperationResult[Message]

}
