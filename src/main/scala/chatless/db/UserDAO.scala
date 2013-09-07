package chatless.db

import chatless._
import chatless.model.{JDoc, User}
import com.mongodb.casbah.Imports._

trait UserDAO extends DAO {
  type ID = UserId
  type Model = User

  def setNick(id: UserId, nick: String) = setOneField(id, User.NICK, nick)

  def setPublic(id: UserId, public: Boolean) = setOneField(id, User.PUBLIC, public)

  def setInfo(id: UserId, info: JDoc) = setOneField(id, User.INFO, JDocStringTransformer.serialize(info))

  def addFollowing(id: UserId, otherId: UserId) = addToSet(id, User.FOLLOWING, otherId)

  def removeFollowing(id: UserId, otherId: UserId) = removeFromSet(id, User.FOLLOWING, otherId)

  def addFollower(id: UserId, otherId: UserId) = addToSet(id, User.FOLLOWERS, otherId)

  def removeFollower(id: UserId, otherId: UserId) = removeFromSet(id, User.FOLLOWERS, otherId)

  def addBlocked(id: UserId, otherId: UserId) = addToSet(id, User.BLOCKED, otherId)

  def removeBlocked(id: UserId, otherId: UserId) = removeFromSet(id, User.BLOCKED, otherId)

  def addTopic(id: UserId, tid: TopicId) = addToSet(id, User.TOPICS, tid)

  def removeTopic(id: UserId, tid: TopicId) = removeFromSet(id, User.TOPICS, tid)

  def addTag(id: UserId, tag: String) = addToSet(id, User.TAGS, tag)

  def removeTag(id: UserId, tag: String) = removeFromSet(id, User.TAGS, tag)
}
