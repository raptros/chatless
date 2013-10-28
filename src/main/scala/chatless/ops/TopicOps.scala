package chatless.ops
import chatless._
import chatless.db.WriteStat
import chatless.model.JDoc

trait TopicOps {
  def setTitle(cid: UserId, tid: TopicId, value: String): WriteStat

  def setPublic(cid: UserId, tid: TopicId, value: Boolean): WriteStat

  def setInfo(cid: UserId, tid: TopicId, value: JDoc): WriteStat

  def setMuted(cid: UserId, tid: TopicId, value: Boolean): WriteStat

  def kickUser(cid: UserId, tid: TopicId, uid: UserId): WriteStat

  def banUser(cid: UserId, tid: TopicId, uid: UserId): WriteStat

  //def inviteUser

  def addSop(cid: UserId, tid: TopicId, uid: UserId): WriteStat

}
