package chatless.model.topic

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