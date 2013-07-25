package chatless.db
import chatless._
import scalaz.Validation
import argonaut._
import Argonaut._
import scalaz.Validation._
import scalaz.syntax.validation._
import scalaz.syntax.std.boolean._
import chatless.operation.{GetField, OpRes, ResMe, ResUser}

case class UserModel(
    uid:UserId,
    nick:String,
    public:Boolean,
    info:Json,
    following:Set[UserId],
    followers:Set[UserId],
    blocked:Set[UserId],
    topics:Set[TopicId],
    tags:Set[String])
  extends ModelAccess {

  lazy val fields:Map[String, FieldAccess[_]] = Map(
    "uid" -> uid,
    "nick" -> nick,
    "public" -> public,
    "info" -> info,
    "following" -> following,
    "followers" -> followers,
    "blocked" -> blocked,
    "topics" -> topics,
    "tags" -> tags
  )

  val readPublic = Set("uid", "nick", "public")

  lazy val replaceableFields = Map(
    "nick" -> { (n:String) => copy(nick = n) },
    "public" -> { (p:Boolean) => copy(public = p) },
    "info" -> { (i:Json) => copy(info = i) }
  )

  lazy val updatableListFields:Map[String, UpdateListField[_]] = Map(
    "following" -> makeAppender[UserId] { u => copy(following = following + u) } withRemover { u =>
      copy(following = following - u)
    },
    "followers" -> mkUpdateListField[UserId](
      u => copy(followers = followers + u),
      u => copy(followers = followers - u)),
    "blocked" -> mkUpdateListField[UserId](u => copy(blocked = blocked + u), u => copy(blocked = blocked - u)),
    "topics" -> mkUpdateListField[TopicId](t => copy(topics = topics + t), t => copy(topics = topics - t)),
    "tags" -> mkUpdateListField[String](t => copy(tags = tags + t), t => copy(tags = tags - t))
  )

  def resFor(cid:UserId):OpRes = if (cid == uid) ResMe else ResUser(uid)

  def canRead(cid:UserId, field:String) = (readPublic contains field) || public || (followers contains cid)

  def canWrite(cid:UserId, field:String) = cid == uid
}

