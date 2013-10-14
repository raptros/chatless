package chatless.sequencers

import com.google.inject.Inject

import scalaz._
import scalaz.std.list._
import scalaz.syntax.id._

import chatless._
import chatless.model._
import chatless.db.{WriteStat, TopicDAO, UserDAO}
import chatless.responses.UserNotFoundError
import akka.actor.{ActorSelection, ActorRef}
import chatless.wiring.params.LocalEventReceiverSelection

class UserOpSequences @Inject() (
    val userDao: UserDAO,
    val topicDao: TopicDAO,
    @LocalEventReceiverSelection val eventActor: ActorSelection)
  extends Sequencer {

  protected def mkEvent[A](action: Action.Value, id: UserId, field: String, value: A) =
    Event(kind = EventKind.USER_UPDATE, action = action, uid = Some(id), field = Some(field), value = ValueContainer(value))

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

  def setPublic(cid: UserId, value: Boolean) = runSequence {
    wrapWS { userDao.setPublic(cid, value) } flatMap {
      condWriteItem(mkEvent(Action.REPLACE, cid, User.PUBLIC, value))
    }
  }

  def unfollowUser(cid: UserId, uid: UserId) = runSequence { unfollowUserSequence(cid, uid) }

  def unfollowUserSequence(cid: chatless.UserId, uid: chatless.UserId) = for {
    update0 <- wrapWS { userDao.removeFollowing(cid, uid) }
    _ <- condWriteItem(mkEvent(Action.REMOVE, cid, User.FOLLOWING, uid))(update0)
    update1 <- wrapWS { stepEB(update0) { userDao.removeFollower(uid, cid) } }
    _ <- condWriteItem(mkEvent(Action.REMOVE, uid, User.FOLLOWERS, cid))(update1)
  } yield update0

  def blockUser(cid: UserId, uid: UserId) = runSequence { blockUserSequence(cid, uid) }

  def blockUserSequence(cid: UserId, uid: UserId) = for {
    update0 <- wrapWS { userDao.addBlocked(cid, uid) }
    _ <- condWriteItem(mkEvent(Action.ADD, cid, User.BLOCKED, uid))(update0)
    update1 <- wrapWS { stepEB(update0) { userDao.removeFollower(cid, uid) } }
    _ <- condWriteItem(mkEvent(Action.REMOVE, cid, User.FOLLOWERS, uid))(update1)
    update2 <- wrapWS { stepEB(update1) { userDao.removeFollowing(uid, cid) } }
    _ <- condWriteItem(mkEvent(Action.REMOVE, uid, User.FOLLOWING, cid))(update2)
  } yield update0


}
