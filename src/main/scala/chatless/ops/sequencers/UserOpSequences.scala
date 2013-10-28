package chatless.ops.sequencers

import com.google.inject.Inject

import scalaz._
import scalaz.std.list._
import scalaz.syntax.id._

import chatless._
import chatless.model._
import chatless.model.Event.{userUpdate, topicUpdate}

import chatless.db.{WriteStat, TopicDAO, UserDAO}
import chatless.responses.UserNotFoundError
import akka.actor.{ActorSelection, ActorRef}
import chatless.wiring.params.LocalEventReceiverSelection
import chatless.ops.UserOps
import com.google.inject.assistedinject.Assisted

class UserOpSequences @Inject() (
    val userDao: UserDAO,
    val topicDao: TopicDAO,
    @LocalEventReceiverSelection val eventActor: ActorSelection,
    @Assisted val senderRef: ActorRef)
  extends Sequencer
  with UserOps {

  def getOrThrow(uid: UserId) = userDao get uid getOrElse { throw UserNotFoundError(uid) }

  def setNick(cid: UserId, value: String) = runSequence {
    //the withEvent syntax is enabled by the implicit class WritableStat, in package.scala
    userDao.setNick(cid, value) withEvent userUpdate(Action.REPLACE, cid, User.NICK, value)
  }

  def setInfo(cid: UserId, value: JDoc) = runSequence {
    userDao.setInfo(cid, value) withEvent userUpdate(Action.REPLACE, cid, User.INFO, value)
  }

  def setPublic(cid: UserId, value: Boolean) = runSequence {
    userDao.setPublic(cid, value) withEvent userUpdate(Action.REPLACE, cid, User.PUBLIC, value)
  }

  def followUser(cid: UserId, uid: UserId) = runSequence { followUserSequence(cid, uid) }

  def unfollowUser(cid: UserId, uid: UserId) = runSequence { unfollowUserSequence(cid, uid) }

  def removeFollower(cid: UserId, uid: UserId) = runSequence { removeFollowerSequence(cid, uid) }

  def blockUser(cid: UserId, uid: UserId) = runSequence { blockUserSequence(cid, uid) }

  def unblockUser(cid: UserId, uid: UserId) = runSequence {
    userDao.removeBlocked(cid, uid) withEvent userUpdate(Action.REMOVE, cid, User.BLOCKED, uid)
  }

  def joinTopic(cid: UserId, tid: TopicId) = runSequence { joinTopicSequence(cid, tid) }

  def leaveTopic(cid: UserId, value: TopicId) = runSequence { leaveTopicSequence(cid, value) }

  def addTag(cid: UserId, value: String) = runSequence {
    userDao.addTag(cid, value) withEvent userUpdate(Action.ADD, cid, User.TAGS, value)
  }

  def removeTag(cid: UserId, value: String) = runSequence {
    userDao.removeTag(cid, value) withEvent userUpdate(Action.REMOVE, cid, User.TAGS, value)
  }

  def followUserSequence(cid: UserId, uid: UserId) = for {
    update0 <- userDao.addFollowing(cid, uid) withEvent
      userUpdate(Action.ADD, cid, User.FOLLOWING, uid)
    update1 <- update0 step userDao.addFollower(uid, cid) withEvent
      userUpdate(Action.ADD, uid, User.FOLLOWERS, cid)
  } yield update0

  def unfollowUserSequence(cid: chatless.UserId, uid: chatless.UserId) = for {
    update0 <- userDao.removeFollowing(cid, uid) withEvent
      userUpdate(Action.REMOVE, cid, User.FOLLOWING, uid)
    update1 <- update0 step userDao.removeFollower(uid, cid) withEvent
      userUpdate(Action.REMOVE, uid, User.FOLLOWERS, cid)
  } yield update0

  def removeFollowerSequence(cid: UserId, uid: UserId) = for {
    update0 <- userDao.removeFollower(cid, uid) withEvent
      userUpdate(Action.REMOVE, cid, User.FOLLOWERS, uid)
    update1 <- update0 step userDao.removeFollowing(uid, cid) withEvent
      userUpdate(Action.REMOVE, uid, User.FOLLOWING, cid)
  } yield update0

  def blockUserSequence(cid: UserId, uid: UserId) = for {
    update0 <- userDao.addBlocked(cid, uid) withEvent
      userUpdate(Action.ADD, cid, User.BLOCKED, uid)
    update1 <- update0 step userDao.removeFollower(cid, uid) withEvent
      userUpdate(Action.REMOVE, cid, User.FOLLOWERS, uid)
    update2 <- update1 step userDao.removeFollowing(uid, cid) withEvent
      userUpdate(Action.REMOVE, uid, User.FOLLOWING, cid)
  } yield update0

  def joinTopicSequence(cid: chatless.UserId, tid: TopicId) = for {
    update0 <- userDao.addTopic(cid, tid) withEvent
      userUpdate(Action.ADD, cid, User.TOPICS, tid)
    //todo this topic event should have cid set to whoever accepted the join request if this isn't a public topic
    update1 <- update0 step topicDao.addParticipant(tid, cid) withEvent
      topicUpdate(Action.ADD, cid, tid, Topic.PARTICIPATING, cid)
  } yield update0

  def leaveTopicSequence(cid: UserId, tid: TopicId) = for {
    update0 <- userDao.removeTopic(cid, tid) withEvent
      userUpdate(Action.REMOVE, cid, User.TOPICS, tid)
    update1 <- update0 step topicDao.removeParticipant(tid, cid) withEvent
      topicUpdate(Action.REMOVE, cid, tid, Topic.PARTICIPATING, cid)
  } yield update0

}
