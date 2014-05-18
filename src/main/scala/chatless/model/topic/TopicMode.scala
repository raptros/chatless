package chatless.model.topic
import argonaut._
import Argonaut._

/**
  * @param muted only the owner and voiced members can post
  * @param public when false, users start with mode !read !write
  */
case class TopicMode(
  muted: Boolean,
  public: Boolean
  )

/** contains default etc modes for topics, along with json codec*/
object TopicMode {
  implicit val topicModeCodec = casecodec2(TopicMode.apply, TopicMode.unapply)("muted", "public")

  /** a good default for the kinds of topics most users will create */
  lazy val default = TopicMode(
    muted = false,
    public = true
  )
}
