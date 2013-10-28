package chatless.ops.sequencers

import chatless._
import chatless.{UserId, TopicId}

import chatless.ops.TopicOps
import akka.actor.{ActorRef, ActorSelection}
import chatless.wiring.params.LocalEventReceiverSelection
import chatless.db._
import chatless.db.WriteStat
import com.google.inject.Inject
import chatless.model._
import chatless.model.Event
import chatless.model.Event.{userUpdate, topicUpdate}
import scalaz._
import scalaz.std.list._
import com.google.inject.assistedinject.Assisted

class TopicOpSequences @Inject() (
    val userDao: UserDAO,
    val topicDao: TopicDAO,
    @LocalEventReceiverSelection val eventActor: ActorSelection,
    @Assisted val senderRef: ActorRef)
  extends Sequencer
  with TopicOps {

  def setTitle(cid: UserId, tid: TopicId, value: String): WriteStat = runSequence {
    topicDao.setTitle(tid, value) withEvent topicUpdate(Action.REPLACE, cid, tid, Topic.TITLE, value)
  }

  def setPublic(cid: UserId, tid: TopicId, value: Boolean): WriteStat = runSequence {
    topicDao.setPublic(tid, value) withEvent topicUpdate(Action.REPLACE, cid, tid, Topic.PUBLIC, value)
  }

  def setInfo(cid: UserId, tid: TopicId, value: JDoc): WriteStat = runSequence {
    topicDao.setInfo(tid, value) withEvent topicUpdate(Action.REPLACE, cid, tid, Topic.INFO, value)
  }

  def setMuted(cid: UserId, tid: TopicId, value: Boolean): WriteStat = runSequence {
    topicDao.setMuted(tid, value) withEvent topicUpdate(Action.REPLACE, cid, tid, Topic.MUTED, value)
  }

  def kickUser(cid: UserId, tid: TopicId, uid: UserId): WriteStat = runSequence { kickUserSequence(cid, tid, uid) }

  def banUser(cid: UserId, tid: TopicId, uid: UserId): WriteStat = runSequence { banUserSequence(cid, tid, uid) }

  def addSop(cid: UserId, tid: TopicId, uid: UserId): WriteStat = runSequence {
    topicDao.addSop(tid, uid) withEvent topicUpdate(Action.ADD, cid, tid, Topic.SOPS, uid)
  }

  def kickUserSequence(cid: UserId, tid: TopicId, uid: UserId) = for {
    update0 <-  topicDao.removeParticipant(tid, uid) withEvent
      topicUpdate(Action.REMOVE, cid, tid, Topic.PARTICIPATING, uid)
    update1 <- update0 step userDao.removeTopic(uid, tid) withEvent
      userUpdate(Action.REMOVE, uid, User.TOPICS, tid)
  } yield update0

  def banUserSequence(cid: UserId, tid: TopicId, uid: UserId) = for {
    update0 <- topicDao.addBanned(tid, uid) withEvent
      topicUpdate(Action.ADD, cid, tid, Topic.BANNED, uid)
    updateA1 <- update0 step topicDao.removeParticipant(tid, uid) withEvent
      topicUpdate(Action.REMOVE, cid, tid, Topic.PARTICIPATING, uid)
    updateA2 <- updateA1 step userDao.removeTopic(uid, tid) withEvent
      userUpdate(Action.REMOVE, uid, User.TOPICS, tid)
    updateB1 <- update0 step topicDao.removeSop(tid, uid) withEvent
      topicUpdate(Action.REMOVE, cid, tid, Topic.SOPS, uid)
    updateC1 <- update0 step topicDao.removeVoiced(tid, uid) withEvent
      topicUpdate(Action.REMOVE, cid, tid, Topic.VOICED, uid)
  } yield update0
}
