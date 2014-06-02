package chatless.model.topic

import argonaut._
import chatless.macros.JsonMacros

/** the mode of a [[Member]] of a topic
  * @param voiced as long as `write`, if the topic is `muted`, this member can still post messages
  * @param read when false, a user cannot see any of the topic's resources - no messages, no docs, no files,
  *             not even members
  * @param write when false, a user is not permitted to post anything at all, even if `voiced`.
  */
case class MemberMode(
  voiced: Boolean,
  read: Boolean,
  write: Boolean)

object MemberMode {
  /** this is the mode given to the creating member of a topic */
  lazy val creator = MemberMode(
    voiced = true,
    read = true,
    write = true
  )

  lazy val joinedPrivate = MemberMode(
    voiced = false,
    read = false,
    write = false
  )

  //todo
  def joinerMode(topicMode: TopicMode): MemberMode =
    MemberMode(
      voiced = false,
      read = topicMode.readable,
      write = topicMode.writable
    )

  //todo
  def invitedMode(topicMode: TopicMode): MemberMode =
    MemberMode(voiced = false, read = true, write = true)

  implicit val memberModeCodecJson = JsonMacros.deriveCaseCodecJson[MemberMode]
}