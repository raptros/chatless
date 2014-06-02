package chatless.model.topic
import argonaut._
import Argonaut._
import chatless.macros.JsonMacros

/** topic mode controls both the overall access of a topic and the capabilities of new members.
  * @param readable whether new members are able to `read`.
  * @param writable whether new members are able to `write`.
  * @param muted members must have the `voiced` capability in order to send to a muted topic.
  * @param members whether users must have a membership in order to read this topic. ignored when not `readable`.
  * @param authenticated whether this topic must be viewed using the authenticated api. requires that `members` is false to have an effect.
  */
case class TopicMode(
  readable: Boolean,
  writable: Boolean,
  muted: Boolean,
  members: Boolean,
  authenticated: Boolean
  )

/** contains default etc modes for topics, along with json codec*/
object TopicMode {
  implicit val topicModeCodec = JsonMacros.deriveCaseCodecJson[TopicMode]

  /** a good default for the kinds of topics most users will create */
  lazy val default = TopicMode(
    readable = true,
    writable = true,
    muted = false,
    members = true,
    authenticated = true
  )
}
