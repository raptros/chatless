package chatless.db
import chatless._
import chatless.op2._
import scala.concurrent.Future

trait DatabaseAccessor {
  def getUser(cid:UserId, uid:UserId):Future[UserM]
}
