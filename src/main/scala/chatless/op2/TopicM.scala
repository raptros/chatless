package chatless.op2
import chatless._
import argonaut.{CodecJson, Json}

case class TopicM(
    tid:TopicId,
    title:String,
    public:Boolean,
    info:Json,
    op:UserId,
    sops:Set[UserId],
    participating:Set[UserId],
    tags:List[String])
  extends AccessModel

object TopicM {
  val TID = "tid"
  val TITLE = "title"
  val PUBLIC = "public"
  val INFO = "info"
  val OP = "op"
  val SOPS = "sops"
  val PARTICIPATING = "participating"
  val TAGS = "tags"

  implicit def TopicMCodecJ:CodecJson[TopicM] =
    CodecJson.casecodec8(TopicM.apply, TopicM.unapply)(TID, TITLE, PUBLIC, INFO, OP, SOPS, PARTICIPATING, TAGS)
}
