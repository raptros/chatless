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
import chatless.model.Event.{userUpdate, topicUpdate, topicCreate, messageCreate}
import scalaz._
import scalaz.syntax.id._
import scalaz.std.list._
import com.google.inject.assistedinject.Assisted
import chatless.responses.TopicNotFoundError
import chatless.model.inits.TopicInit
import scala.util.Random
import org.joda.time.DateTime

class TopicOpSequences @Inject() (
    val userDao: UserDAO,
    val topicDao: TopicDAO,
    val messageDao: MessageDAO,
    @LocalEventReceiverSelection val eventActor: ActorSelection,
    @Assisted val senderRef: ActorRef)
  extends Sequencer
  with TopicOps {

  def getOrThrow(tid: TopicId) = topicDao get tid getOrElse { throw TopicNotFoundError(tid) }


  def createTopic(cid: UserId, init: TopicInit) = {
    val initTopic = initTopicFrom(cid, init)
    runSequence {
      createTopicSequence(initTopic, init.invite)
    } flatMap { res =>
      if (res) initTopic.id.right else "couldn't save topic!".left
    }
  }

  def setTitle(cid: UserId, tid: TopicId, value: String) = runSequence {
    topicDao.setTitle(tid, value) withEvent topicUpdate(Action.REPLACE, cid, tid, Topic.TITLE, value)
  }

  def setPublic(cid: UserId, tid: TopicId, value: Boolean) = runSequence {
    topicDao.setPublic(tid, value) withEvent topicUpdate(Action.REPLACE, cid, tid, Topic.PUBLIC, value)
  }

  def setInfo(cid: UserId, tid: TopicId, value: JDoc) = runSequence {
    topicDao.setInfo(tid, value) withEvent topicUpdate(Action.REPLACE, cid, tid, Topic.INFO, value)
  }

  def setMuted(cid: UserId, tid: TopicId, value: Boolean) = runSequence {
    topicDao.setMuted(tid, value) withEvent topicUpdate(Action.REPLACE, cid, tid, Topic.MUTED, value)
  }

  def kickUser(cid: UserId, tid: TopicId, uid: UserId) = runSequence { kickUserSequence(cid, tid, uid) }

  def banUser(cid: UserId, tid: TopicId, uid: UserId) = runSequence { banUserSequence(cid, tid, uid) }

  def unbanUser(cid: UserId, tid: TopicId, uid: UserId) = runSequence {
    topicDao.removeBanned(tid, uid) withEvent topicUpdate(Action.REMOVE, cid, tid, Topic.BANNED, uid)
  }

  def addSop(cid: UserId, tid: TopicId, uid: UserId) = runSequence {
    topicDao.addSop(tid, uid) withEvent topicUpdate(Action.ADD, cid, tid, Topic.SOPS, uid)
  }

  def removeSop(cid: UserId, tid: TopicId, uid: UserId) = runSequence {
    topicDao.removeSop(tid, uid) withEvent topicUpdate(Action.REMOVE, cid, tid, Topic.SOPS, uid)
  }

  def addVoiced(cid: UserId, tid: TopicId, uid: UserId) = runSequence {
    topicDao.addVoiced(tid, uid) withEvent topicUpdate(Action.ADD, cid, tid, Topic.VOICED, uid)
  }

  def removeVoiced(cid: UserId, tid: TopicId, uid: UserId) = runSequence {
    topicDao.removeVoiced(tid, uid) withEvent topicUpdate(Action.REMOVE, cid, tid, Topic.VOICED, uid)
  }

  def addTag(cid: UserId, tid: TopicId, tag: String) = runSequence {
    topicDao.addTag(tid, tag) withEvent topicUpdate(Action.ADD, cid, tid, Topic.TAGS, tag)
  }

  def removeTag(cid: UserId, tid: TopicId, tag: String) = runSequence {
    topicDao.addTag(tid, tag) withEvent topicUpdate(Action.REMOVE, cid, tid, Topic.TAGS, tag)
  }

  def sendMessage(cid: UserId, tid: TopicId, body: JDoc) = {
    val initMessage = initMessageFrom(cid, tid, body)
    runSequence {
      messageDao.saveNewMessage(initMessage) withEvent messageCreate(initMessage)
    } flatMap { res =>
      if (res) initMessage.id.right else "couldn't save message!".left
    }
  }

  //todo fix this stuff!
  def initMessageFrom(cid: UserId, tid: TopicId, body: JDoc) = Message(
    id = tid + Random.nextString(10), //bad!
    tid = tid,
    uid = cid,
    timestamp = DateTime.now(),
    body = body
  )

  //todo fix the heck out of this!
  def initTopicFrom(cid: UserId, init: TopicInit) = Topic(
    id = Random.nextString(33), //bad!
    title = init.title,
    public = init.public,
    muted = init.muted,
    info = init.info,
    op = cid,
    sops = Set.empty[UserId],
    voiced = Set.empty[UserId],
    users = Set.empty[UserId],
    banned = Set.empty[UserId],
    tags = init.tags)

  def createTopicSequence(topic: Topic, invite: Set[String]) = for {
    create <- topicDao.saveNewTopic(topic) withEvent
      topicCreate(topic)
    added <- create step topicDao.addUser(topic.id, topic.op) withEvent
      topicUpdate(Action.ADD, topic.op, topic.id, Topic.USERS, topic.op)
    joined <- added step userDao.addTopic(topic.op, topic.id) withEvent
      userUpdate(Action.ADD, topic.op, User.TOPICS, topic.id)
    //todo send invites.
  } yield create

  def kickUserSequence(cid: UserId, tid: TopicId, uid: UserId) = for {
    update0 <-  topicDao.removeUser(tid, uid) withEvent
      topicUpdate(Action.REMOVE, cid, tid, Topic.USERS, uid)
    update1 <- update0 step userDao.removeTopic(uid, tid) withEvent
      userUpdate(Action.REMOVE, uid, User.TOPICS, tid)
  } yield update0

  def banUserSequence(cid: UserId, tid: TopicId, uid: UserId) = for {
    update0 <- topicDao.addBanned(tid, uid) withEvent
      topicUpdate(Action.ADD, cid, tid, Topic.BANNED, uid)
    updateA1 <- update0 step topicDao.removeUser(tid, uid) withEvent
      topicUpdate(Action.REMOVE, cid, tid, Topic.USERS, uid)
    updateA2 <- updateA1 step userDao.removeTopic(uid, tid) withEvent
      userUpdate(Action.REMOVE, uid, User.TOPICS, tid)
    updateB1 <- update0 step topicDao.removeSop(tid, uid) withEvent
      topicUpdate(Action.REMOVE, cid, tid, Topic.SOPS, uid)
    updateC1 <- update0 step topicDao.removeVoiced(tid, uid) withEvent
      topicUpdate(Action.REMOVE, cid, tid, Topic.VOICED, uid)
  } yield update0
}
