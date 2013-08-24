package chatless.db
import chatless._
import chatless.op2._
import scala.concurrent.{ExecutionContext, Future}
import argonaut.{Json, CodecJson}
import chatless.models._

trait DatabaseAccessor {
  def getUser(cid: UserId, uid: UserId)(implicit context: ExecutionContext): Future[UserM]

  def updateUser(cid: UserId, uid: UserId, spec: UpdateSpec with ForUsers)(implicit context: ExecutionContext): Future[Boolean]

  def getTopic(cid: UserId, tid: TopicId)(implicit context: ExecutionContext): Future[TopicM]

  def updateTopic(cid: UserId, tid: TopicId, spec: UpdateSpec with ForTopics)(implicit context: ExecutionContext): Future[Boolean]

  def getMessages(cid: UserId, tid: TopicId, spec: GetRelative with ForMessages)(implicit context: ExecutionContext): Future[List[MessageM]]

  def createMessage(cid: UserId, tid: TopicId, j: Json)(implicit context: ExecutionContext): Future[CreateHandle]

  def getEvents(cid: UserId, spec: GetRelative with ForEvents)(implicit context: ExecutionContext): Future[List[Event]]
}
