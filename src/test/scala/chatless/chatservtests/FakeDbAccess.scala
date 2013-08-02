package chatless.chatservtests
import chatless._

import chatless.op2.{TopicM, UserM, AccessModel}
import shapeless._
import Typeable._
import chatless.db.DatabaseAccessor
import scala.concurrent._

case class FakeDbAccess(models:AccessModel*)(implicit context:ExecutionContext) extends DatabaseAccessor {
  var users = (models flatMap { _.cast[UserM] } map { u => u.uid -> u }).toMap
  var topics = (models flatMap { _.cast[TopicM] } map { t => t.tid -> t }).toMap

  def getUser(cid:UserId, uid:UserId) = future { users(uid) }

}
