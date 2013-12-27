package chatless.ops
import chatless._
import chatless.db.WriteStat
import chatless.model.{Topic, JDoc}
import chatless.model.inits.TopicInit
import scalaz.\/

trait TopicOps {
  def getOrThrow(tid: TopicId): Topic

  def createTopic(cid: UserId, init: TopicInit): String \/ TopicId

  def setTitle(cid: UserId, tid: TopicId, value: String): WriteStat

  def setPublic(cid: UserId, tid: TopicId, value: Boolean): WriteStat

  def setInfo(cid: UserId, tid: TopicId, value: JDoc): WriteStat

  def setMuted(cid: UserId, tid: TopicId, value: Boolean): WriteStat

  def kickUser(cid: UserId, tid: TopicId, uid: UserId): WriteStat

  def banUser(cid: UserId, tid: TopicId, uid: UserId): WriteStat

  def unbanUser(cid: UserId, tid: TopicId, uid: UserId): WriteStat

  def addSop(cid: UserId, tid: TopicId, uid: UserId): WriteStat

  def removeSop(cid: UserId, tid: TopicId, uid: UserId): WriteStat

  def addVoiced(cid: UserId, tid: TopicId, uid: UserId): WriteStat

  //def inviteUser

  def removeVoiced(cid: UserId, tid: TopicId, uid: UserId): WriteStat

  def addTag(cid: UserId, tid: TopicId, tag: String): WriteStat

  def removeTag(cid: UserId, tid: TopicId, tag: String): WriteStat

  def sendMessage(cid: UserId, tid: TopicId, body: JDoc): String \/ MessageId
}
