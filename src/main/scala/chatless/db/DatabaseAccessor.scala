package chatless.db
import chatless._
import chatless.op2._
import scala.concurrent.Future
import argonaut.CodecJson

trait DatabaseAccessor {
  def getUser(cid:UserId, uid:UserId):Future[UserM]

  def updateUser(cid:UserId, uid:UserId, spec: UpdateSpec with ForUsers):Future[Boolean]

  def getTopic(cid:UserId, tid:TopicId):Future[TopicM]

  def updateTopic(cid:UserId, tid:TopicId, spec: UpdateSpec with ForTopics):Future[Boolean]
}
