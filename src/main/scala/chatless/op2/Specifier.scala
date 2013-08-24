package chatless.op2
import chatless._
import argonaut.Json

sealed trait Specifier

trait ForUsers { this: Specifier => }
trait ForTopics { this: Specifier => }
trait ForMessages { this: Specifier => }
trait ForEvents { this: Specifier => }

sealed trait GetSpec extends Specifier

case object GetRes extends GetSpec with ForUsers with ForTopics

sealed abstract class GetRelative(val forward: Boolean, val baseIdOption: OptPair[String, Boolean], val count: Int) extends GetSpec

object GetRelative {
  def apply(forward: Boolean, baseIdOption: OptPair[String, Boolean], count: Int = 1): GetRelative = (forward, baseIdOption) match {
    case (true, None) => GetFirst(count)
    case (false, None) => GetLast(count)
    case (false, Some(Pair(baseId, true)))  => GetAt(baseId, count)
    case (false, Some(Pair(baseId, false))) => GetBefore(baseId, count)
    case (true, Some(Pair(baseId, true)))   => GetFrom(baseId, count)
    case (true, Some(Pair(baseId, false)))  => GetAfter(baseId, count)
  }

  def unapply(gr: GetRelative): Option[(Boolean, OptPair[String, Boolean], Int)] =
    Some(gr.forward, gr.baseIdOption, gr.count)
}

case class GetFirst(c: Int = 1)
  extends GetRelative(true, None, c)
  with ForMessages

case class GetLast(c: Int = 1)
  extends GetRelative(true, None, c)
  with ForMessages
  with ForEvents

case class GetAt(baseId: String, c: Int = 1)
  extends GetRelative(false, Some(baseId -> true),  c)
  with ForMessages
  with ForEvents

case class GetBefore(baseId: String, c: Int = 1)
  extends GetRelative(false, Some(baseId -> false), c)
  with ForMessages
  with ForEvents

case class GetFrom(baseId: String, c: Int = 1)
  extends GetRelative(true,  Some(baseId -> true),  c)
  with ForMessages
  with ForEvents

case class GetAfter(baseId: String, c: Int = 1)
  extends GetRelative(true,  Some(baseId -> false), c)
  with ForMessages
  with ForEvents

sealed abstract class UpdateSpec extends Specifier

/** applies to users */
case class ReplaceNick(nick: String)
  extends UpdateSpec
  with ForUsers

/** applies to users and topics */
case class SetPublic(public: Boolean)
  extends UpdateSpec
  with ForUsers
  with ForTopics

/** applies to users and topics */
case class UpdateInfo(info: Json)
  extends UpdateSpec
  with ForUsers with
  ForTopics

/** applies to users. requires a request. */
case class FollowUser(user: UserId, additional: Option[Json] = None)
  extends UpdateSpec
  with ForUsers

/** applies to users */
case class UnfollowUser(user: UserId)
  extends UpdateSpec
  with ForUsers

/** applies to users */
case class RemoveFollower(user: UserId)
  extends UpdateSpec
  with ForUsers

/** applies to users and topics */
case class BlockUser(user: UserId)
  extends UpdateSpec
  with ForUsers
  with ForTopics

/** applies to users */
case class UnblockUser(user: UserId)
  extends UpdateSpec
  with ForUsers
  with ForTopics

/** applies to users. requires a request.*/
case class JoinTopic(topic: TopicId, additional: Option[Json] = None)
  extends UpdateSpec
  with ForUsers

/** applies to users */
case class LeaveTopic(topic: TopicId)
  extends UpdateSpec with ForUsers

/** applies to users and topics */
case class AddTag(tag: String)
  extends UpdateSpec
  with ForUsers
  with ForTopics

/** applies to users and topics */
case class RemoveTag(tag: String)
  extends UpdateSpec
  with ForUsers
  with ForTopics

case class ChangeTitle(title: String)
  extends UpdateSpec
  with ForTopics

case class InviteUser(uid: UserId, additional: Option[Json] = None)
  extends UpdateSpec
  with ForTopics

case class KickUser(uid: UserId)
  extends UpdateSpec
  with ForTopics

case class PromoteSop(uid: UserId)
  extends UpdateSpec
  with ForTopics

case class DemoteSop(uid: UserId)
  extends UpdateSpec
  with ForTopics


sealed abstract class Create extends Specifier

case class CreateMessage(messageJson: Json)
  extends Create
  with ForMessages