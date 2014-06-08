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
  write: Boolean,
  invite: Boolean,
  setMember: Boolean,
  setBanner: Boolean,
  setInfo: Boolean,
  setMode: Boolean)

object MemberMode {
  /** this is the mode given to the creating member of a topic */
  lazy val creator = MemberMode(
    voiced = true,
    read = true,
    write = true,
    invite = true,
    setMember = true,
    setBanner = true,
    setInfo = true,
    setMode = true
  )

  lazy val modeDeny = MemberMode(
    voiced = false,
    read = false,
    write = false,
    invite = false,
    setMember = false,
    setBanner = false,
    setInfo = false,
    setMode = false
  )

  def nonMemberMode(mode: TopicMode) = modeDeny.copy(read = mode.readable && !mode.members)

  //todo
  def joinerMode(topicMode: TopicMode): MemberMode =
    modeDeny.copy(read = topicMode.readable, write = topicMode.writable)

  //todo
  def invitedMode(topicMode: TopicMode): MemberMode = modeDeny.copy(read = true, write = topicMode.writable)

  implicit val memberModeCodecJson = JsonMacros.deriveCaseCodecJson[MemberMode]
}