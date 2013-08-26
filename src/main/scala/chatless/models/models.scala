package chatless.models

import chatless._

import com.novus.salat.annotations._

import org.joda.time.DateTime
import org.bson.types.ObjectId

@Salat
trait BaseModel {
//  @Persist @Key("_id") val id: ObjectId
}


case class User(
    uid: UserId,
    nick: String,
    public: Boolean,
    info: Map[String, Any],
    following: Set[UserId],
    followers: Set[UserId],
    blocked: Set[UserId],
    topics: Set[TopicId],
    tags: Set[String])
  extends BaseModel

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

  lazy val allFields =
    (  UID
    :: NICK
    :: PUBLIC
    :: INFO
    :: FOLLOWING
    :: FOLLOWERS
    :: BLOCKED
    :: TOPICS
    :: TAGS
    :: Nil
    )

  lazy val followerFields =
    (  UID
    :: NICK
    :: PUBLIC
    :: INFO
    :: FOLLOWING
    :: FOLLOWERS
    :: Nil
    )

  lazy val publicFields =
    (  UID
    :: NICK
    :: PUBLIC
    :: Nil
    )

  lazy val callerOnlyFields = allFields diff followerFields

  lazy val nonPublicFields = allFields diff publicFields
}

case class Topic(
    tid: TopicId,
    title: String,
    public: Boolean,
    info: Map[String, Any],
    op: UserId,
    sops: Set[UserId],
    participating: Set[UserId],
    tags: Set[String])
  extends BaseModel

object Topic {
  val TID = "name"
  val TITLE = "title"
  val PUBLIC = "public"
  val INFO = "info"
  val OP = "op"
  val SOPS = "sops"
  val PARTICIPATING = "participating"
  val TAGS = "tags"
}
