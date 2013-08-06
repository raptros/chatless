package chatless.op2
import chatless._
import argonaut.Json

sealed trait Specifier

sealed trait GetSpec extends Specifier

case object GetRes extends GetSpec

case class GetRelative(forward: Boolean = false, inclusive: Boolean = true, baseId: Option[String] = None, count: Int = 1) extends GetSpec

sealed abstract class UpdateSpec extends Specifier

sealed trait ForUsers { this: UpdateSpec => }

sealed trait ForTopics { this: UpdateSpec => }

/** applies to users */
case class ReplaceNick(nick: String) extends UpdateSpec with ForUsers

/** applies to users and topics */
case class SetPublic(public: Boolean) extends UpdateSpec with ForUsers with ForTopics

/** applies to users and topics */
case class UpdateInfo(info: Json) extends UpdateSpec with ForUsers with ForTopics

/** applies to users. requires a request. */
case class FollowUser(user: UserId, additional: Option[Json] = None) extends UpdateSpec with ForUsers

/** applies to users */
case class UnfollowUser(user: UserId) extends UpdateSpec with ForUsers

/** applies to users */
case class RemoveFollower(user: UserId) extends UpdateSpec with ForUsers

/** applies to users and topics */
case class BlockUser(user: UserId) extends UpdateSpec with ForUsers with ForTopics

/** applies to users */
case class UnblockUser(user: UserId) extends UpdateSpec with ForUsers with ForTopics

/** applies to users. requires a request.*/
case class JoinTopic(topic: TopicId, additional: Option[Json] = None) extends UpdateSpec with ForUsers

/** applies to users */
case class LeaveTopic(topic: TopicId) extends UpdateSpec with ForUsers

/** applies to users and topics */
case class AddTag(tag: String) extends UpdateSpec with ForUsers with ForTopics

/** applies to users and topics */
case class RemoveTag(tag: String) extends UpdateSpec with ForUsers with ForTopics

case class ChangeTitle(title: String) extends UpdateSpec with ForTopics

case class InviteUser(uid: UserId, additional: Option[Json] = None) extends UpdateSpec with ForTopics

case class KickUser(uid: UserId) extends UpdateSpec with ForTopics

case class PromoteSop(uid: UserId) extends UpdateSpec with ForTopics

case class DemoteSop(uid: UserId) extends UpdateSpec with ForTopics

