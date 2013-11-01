package chatless.model

import chatless._
import org.json4s._
import com.novus.salat.annotations._

case class User(
    @Key("_id")
    id:         UserId,
    nick:       String,
    public:     Boolean,
    info:       JDoc,
    following:  Set[UserId],
    followers:  Set[UserId],
    blocked:    Set[UserId],
    topics:     Set[TopicId],
    tags:       Set[String]) {
  import User._

  def getFields(fields: Set[String]): Map[String, Any] = {
    (ID         -> id) ::
    (NICK       -> nick) ::
    (PUBLIC     -> public) ::
    (INFO       -> info) ::
    (FOLLOWING  -> following) ::
    (FOLLOWERS  -> followers) ::
    (BLOCKED    -> blocked) ::
    (TOPICS     -> topics) ::
    (TAGS       -> tags) ::
    Nil
  }.toMap filterKeys { fields.contains }
}

object User {
  val ID        = "id"
  val NICK      = "nick"
  val PUBLIC    = "public"
  val INFO      = "info"
  val FOLLOWING = "following"
  val FOLLOWERS = "followers"
  val BLOCKED   = "blocked"
  val TOPICS    = "topics"
  val TAGS      = "tags"

  lazy val allFields = {
    ID :: NICK :: PUBLIC :: INFO :: FOLLOWING :: FOLLOWERS :: BLOCKED :: TOPICS :: TAGS :: Nil
  }.toSet

  lazy val followerFields = {
    ID :: NICK :: PUBLIC :: INFO :: FOLLOWING :: FOLLOWERS :: TOPICS :: Nil
  }.toSet

  lazy val publicFields = {
    ID :: NICK :: PUBLIC :: Nil
  }.toSet

  lazy val callerOnlyFields = allFields diff followerFields

  lazy val nonPublicFields = allFields diff publicFields
}

