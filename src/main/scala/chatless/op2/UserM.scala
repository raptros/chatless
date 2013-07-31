package chatless.op2
import chatless._
import argonaut.{Json, CodecJson}

case class UserM(
    uid:UserId,
    nick:String,
    public:Boolean,
    info:Json,
    following:List[UserId],
    followers:List[UserId],
    blocked:List[UserId],
    topics:List[TopicId],
    tags:List[String])
  extends AccessModel {

}

object UserM {
  implicit def UserMCodecJ:CodecJson[UserM] = CodecJson.casecodec9(UserM.apply, UserM.unapply)(
    "uid", "nick", "public", "info", "following", "followers", "blocked", "topics", "tags")
}
