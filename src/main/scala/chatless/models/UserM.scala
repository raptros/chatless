package chatless.models

import chatless._
import argonaut.{EncodeJson, Json, CodecJson}

case class UserM(
    uid:UserId,
    nick:String,
    public:Boolean,
    info:Json,
    following:Set[UserId],
    followers:Set[UserId],
    blocked:Set[UserId],
    topics:Set[TopicId],
    tags:Set[String])
  extends AccessModel

object UserM {
  val UID = "uid"
  val NICK = "nick"
  val PUBLIC = "public"
  val INFO = "info"
  val FOLLOWING = "following"
  val FOLLOWERS = "followers"
  val BLOCKED = "blocked"
  val TOPICS = "topics"
  val TAGS = "tags"

  implicit def UserMCodecJ:CodecJson[UserM] =
    CodecJson.casecodec9(UserM.apply, UserM.unapply)(UID, NICK, PUBLIC, INFO, FOLLOWING, FOLLOWERS, BLOCKED, TOPICS, TAGS)

}


