package chatless.op2
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
  extends AccessModel {


}

object UserM {
  implicit def UserMCodecJ:CodecJson[UserM] = CodecJson.casecodec9(UserM.apply, UserM.unapply)(
    "uid", "nick", "public", "info", "following", "followers", "blocked", "topics", "tags")
}

