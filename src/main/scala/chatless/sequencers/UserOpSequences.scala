package chatless.sequencers

import com.google.inject.Inject

import scalaz._
import scalaz.std.list._
import scalaz.syntax.id._

import chatless._
import chatless.model._
import chatless.db.{WriteStat, TopicDAO, UserDAO}
import chatless.responses.UserNotFoundError
import akka.actor.ActorRef

class UserOpSequences @Inject() (
    val userDao: UserDAO,
    val topicDao: TopicDAO)
  extends Sequencer {

  def mkEvent[A](action: Action.Value, id: UserId, field: String, value: A) =
    Event(kind = EventKind.USER_UPDATE, action = action, uid = Some(id), field = Some(field), value = ValueContainer(value))

  def blockUserSequence(cid: UserId, uid: UserId) = for {
    update0 <- wrapWS { userDao.addBlocked(cid, uid) }
    _ <- condWriteItem(mkEvent(Action.ADD, cid, User.BLOCKED, uid))(update0)
    update1 <- wrapWS { stepEither(update0, update0) { userDao.removeFollower(cid, uid) } }
    _ <- condWriteItem(mkEvent(Action.REMOVE, cid, User.FOLLOWERS, uid))(update1)
    update2 <- wrapWS { stepEither(update1, update1) { userDao.removeFollowing(uid, cid) } }
    _ <- condWriteItem(mkEvent(Action.REMOVE, uid, User.FOLLOWING, cid))(update2)
  } yield update0


  def blockUser(cid: UserId, uid: UserId): WriteStat = runSequence { blockUserSequence(cid, uid) }

  def setNick(cid: UserId, value: String) = runSequence {
    wrapWS { userDao.setNick(cid, value) } flatMap {
      condWriteItem(mkEvent(Action.REPLACE, cid, User.NICK, value))
    }
  }

  def setInfo(cid: UserId, value: JDoc) = runSequence {
    wrapWS { userDao.setInfo(cid, value) } flatMap {
      condWriteItem(mkEvent(Action.REPLACE, cid, User.INFO, value))
    }
  }


}
