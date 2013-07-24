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
  tags:Set[String]) extends ModelAccess {

  val fields:Map[String, FieldAccess[_]] = Map(
    "uid" -> uid,
    "nick" -> nick,
    "public" -> public,
    "info" -> info,
    "following" -> following,
    "following" -> following,
    "followers" -> followers,
    "blocked" -> blocked,
    "topics" -> topics,
    "tags" -> tags
  )

  val replaceableFields = Map(
    "nick" -> { (n:String) => copy(nick = n) },
    "public" -> { (p:Boolean) => copy(public = p) },
    "info" -> { (i:Json) => copy(info = i) }
  )

  def updatableListFields:Map[String, UpdateListField[_]] = Map(
    "following" -> updateListField[UserId](u => copy(following = following + u), u => copy(following = following - u)),
    "blocked" -> updateListField[UserId](u => copy(blocked = blocked + u), u => copy(blocked = blocked - u)),
    "tags" -> updateListField[String](u => copy(tags = tags + u), u => copy(tags = tags - u))
  )


  def resFor(cid:UserId):OpRes = if (cid == uid) ResMe else ResUser(uid)

  def canRead(cid:UserId, field:String) = (UserModel.readPublic contains field) || public || (followers contains cid)

  def canWrite(cid:UserId, field:String) = cid == uid
}

object UserModel {
  val readPublic = Set("uid", "nick", "public")

}
