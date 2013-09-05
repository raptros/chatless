package chatless.db

import chatless._
import chatless.model.{JDoc, Topic}
import com.mongodb.casbah.Imports._

trait TopicDAO extends DAO {
  type ID = TopicId
  type Model = Topic

  implicit val idAsQueryParam: AsQueryParam[ID] = AsQueryParam[String]

  def setTitle(id: TopicId, title: String) = setOneField(id, Topic.TITLE, title)

  def setPublic(id: TopicId, public: Boolean) = setOneField(id, Topic.PUBLIC, public)

  def setInfo(id: TopicId, info: JDoc) = setOneField(id, Topic.INFO, JDocStringTransformer.serialize(info))

  def setOp(id: TopicId, op: UserId) = setOneField(id, Topic.OP, op)

  def addSOp(id: TopicId, sop: UserId) = addToSet(id, Topic.SOPS, sop)

  def removeSop(id: TopicId, sop: UserId) = removeFromSet(id, Topic.SOPS, sop)

  def addParticipant(id: TopicId, part: UserId) = addToSet(id, Topic.PARTICIPATING, part)

  def removeParticipant(id: TopicId, part: UserId) = removeFromSet(id, Topic.PARTICIPATING, part)

  def addTag(id: TopicId, tag: String) = addToSet(id, Topic.TAGS, tag)

  def removeTag(id: TopicId, tag: String) = removeFromSet(id, Topic.TAGS, tag)
}
