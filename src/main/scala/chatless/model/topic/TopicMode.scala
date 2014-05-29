package chatless.model.topic
import argonaut._
import Argonaut._
import chatless.macros.JsonMacros

/**
  * @param muted only the owner and voiced members can post
  * @param open when false, users start with mode !read !write
  * @param public when true, the topic can be read by users who are not members of the topic
  */
case class TopicMode(
  muted: Boolean,
  open: Boolean,
  public: Boolean
  )

/** contains default etc modes for topics, along with json codec*/
object TopicMode {
  implicit val topicModeCodec = JsonMacros.deriveCaseCodecJson[TopicMode]

  /** a good default for the kinds of topics most users will create */
  lazy val default = TopicMode(
    muted = false,
    open = true,
    public = false
  )
}
