package chatless.models

import chatless._

import com.novus.salat.annotations._

import org.joda.time.DateTime
import org.bson.types.ObjectId
import org.json4s._





@Salat
trait BaseModel {
//  @Persist @Key("_id") val id: ObjectId
}


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
//
//
  val uidGet = { u: User => u.uid }
  val nickGet = { u: User => u.nick }
  val publicGet = { u: User => u.public }
  val infoGet = { u: User => u.info }
  val followingGet = { u: User => u.following }
  val followersGet = { u: User => u.followers }
  val blockedGet = { u: User => u.blocked }
  val topicsGet = { u: User => u.topics }
  val tagsGet = { u: User => u.tags }

  val uid = UID -> uidGet
  val nick = NICK -> nickGet
  val public = PUBLIC -> publicGet
  val info = INFO -> infoGet
  val following = FOLLOWING -> followingGet
  val followers = FOLLOWERS -> followersGet
  val blocked = BLOCKED -> blockedGet
  val topics = TOPICS -> topicsGet
  val tags = TAGS -> tagsGet

  lazy val allFieldsMap = Map(uid, nick, public, info, following, followers, blocked, topics, tags)

//  lazy val stringFieldsMap = Map
//
//  lazy val setFieldsMap = Map(following, followers, blocked, topics, tags)


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

case class Topic(
    tid: TopicId,
    title: String,
    public: Boolean,
    info: JObject,
    op: UserId,
    sops: Set[UserId],
    participating: Set[UserId],
    tags: Set[String])
  extends BaseModel {
  import Topic._

  def getFields(fields: Set[String]): Map[String, Any] = {
    (TID -> tid) ::
    (TITLE -> title) ::
    (PUBLIC -> public) ::
    (INFO -> info) ::
    (OP -> op) ::
    (SOPS -> sops) ::
    (PARTICIPATING -> participating) ::
    (TAGS -> tags) ::
    Nil
  }.toMap filterKeys { fields.contains }
}

object Topic {
  val TID = "name"
  val TITLE = "title"
  val PUBLIC = "public"
  val INFO = "info"
  val OP = "op"
  val SOPS = "sops"
  val PARTICIPATING = "participating"
  val TAGS = "tags"

  val publicFields = TID :: TITLE :: PUBLIC :: Nil
  val participantFields = TID :: TITLE :: PUBLIC :: INFO :: OP :: SOPS :: PARTICIPATING :: TAGS :: Nil
}

case class Event(eid: EventId)
  extends BaseModel

object Event {
  val EID = "eid"

}

case class Message(
    mid: MessageId,
    tid: TopicId,
    uid: UserId,
    timestamp: DateTime,
    body: Map[String, Any])
  extends BaseModel


object Message {
  val MID = "mid"
  val TID = "tid"
  val UID = "uid"
  val BODY = "body"
  val TIMESTAMP = "timestamp"
}
