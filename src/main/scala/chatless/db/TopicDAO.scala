package chatless.db

import chatless._
import chatless.model.{JDoc, Topic}
import com.mongodb.casbah.Imports._

trait TopicDAO extends DAO {
  type ID = TopicId
  type Model = Topic

  def setTitle(id: TopicId, title: String) = setOneField(id, Topic.TITLE, title)

  def setPublic(id: TopicId, public: Boolean) = setOneField(id, Topic.PUBLIC, public)

  def setMuted(id: TopicId, muted: Boolean) = setOneField(id, Topic.MUTED, muted)

  def setInfo(id: TopicId, info: JDoc) = setOneField(id, Topic.INFO, JDocStringTransformer.serialize(info))

  def setOp(id: TopicId, op: UserId) = setOneField(id, Topic.OP, op)

  def addSop(id: TopicId, sop: UserId) = addToSet(id, Topic.SOPS, sop)

  def removeSop(id: TopicId, sop: UserId) = removeFromSet(id, Topic.SOPS, sop)

  def addVoiced(id: TopicId, voiced: UserId) = addToSet(id, Topic.VOICED, voiced)

  def removeVoiced(id: TopicId, voiced: UserId) = removeFromSet(id, Topic.VOICED, voiced)

  def addParticipant(id: TopicId, part: UserId) = addToSet(id, Topic.PARTICIPATING, part)

  def removeParticipant(id: TopicId, part: UserId) = removeFromSet(id, Topic.PARTICIPATING, part)

  def addBanned(id: TopicId, banned: UserId) = addToSet(id, Topic.BANNED, banned)

  def removeBanned(id: TopicId, banned: UserId) = removeFromSet(id, Topic.BANNED, banned)

  def addTag(id: TopicId, tag: String) = addToSet(id, Topic.TAGS, tag)

  def removeTag(id: TopicId, tag: String) = removeFromSet(id, Topic.TAGS, tag)
}
