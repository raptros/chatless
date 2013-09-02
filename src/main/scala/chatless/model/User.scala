package chatless.model

import chatless._
import org.json4s._

case class User(
    uid: UserId,
    nick: String,
    public: Boolean,
    info: JObject,
    following: Set[UserId],
    followers: Set[UserId],
    blocked: Set[UserId],
    topics: Set[TopicId],
    tags: Set[String])
  extends BaseModel {
  import User._

  def getFields(fields: Set[String]): Map[String, Any] = {
    (UID -> uid) ::
    (NICK -> nick) ::
    (PUBLIC -> public) ::
    (INFO -> info) ::
    (FOLLOWING -> following) ::
    (FOLLOWERS -> followers) ::
    (BLOCKED -> blocked) ::
    (TOPICS -> topics) ::
    (TAGS -> tags) ::
    Nil
  }.toMap filterKeys { fields.contains }
}

object User {
  val UID = "uid"
  val NICK = "nick"
  val PUBLIC = "public"
  val INFO = "info"
  val FOLLOWING = "following"
  val FOLLOWERS = "followers"
  val BLOCKED = "blocked"
  val TOPICS = "topics"
  val TAGS = "tags"

  lazy val allFields = {
    UID :: NICK :: PUBLIC :: INFO :: FOLLOWING :: FOLLOWERS :: BLOCKED :: TOPICS :: TAGS :: Nil
  }.toSet

  lazy val followerFields = {
    UID :: NICK :: PUBLIC :: INFO :: FOLLOWING :: FOLLOWERS :: TOPICS :: Nil
  }.toSet

  lazy val publicFields = {
    UID :: NICK :: PUBLIC :: Nil
  }.toSet

  lazy val callerOnlyFields = allFields diff followerFields

  lazy val nonPublicFields = allFields diff publicFields
}

