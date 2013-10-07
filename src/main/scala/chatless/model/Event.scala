package chatless.model

import chatless._
import scalaz.syntax.apply._
import scalaz.std.tuple._
import scalaz.std.anyVal._
import scalaz.std.string._
import scalaz.std.option._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._


import com.novus.salat.annotations._
import org.json4s._
import com.novus.salat._
import com.mongodb.casbah.Imports._


case class Event(
    kind: EventKind.Value,
    action: Action.Value,
    @Key("_id") id: Option[EventId] = None,
    parent: Option[EventId] = None,
    uid: Option[UserId] = None,
    cid: Option[UserId] = None,
    tid: Option[TopicId] = None,
    rid: Option[RequestId] = None,
    field: Option[String] = None,
    value: ValueContainer)
/*
sealed abstract class Event {
  val id: Option[ObjectId]
  val parent: Option[ObjectId]
}

case class CreateUser(
    uid: UserId,
    value: String,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserReplaceNick(
    uid: UserId,
    value: String,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserReplacePublic(
    uid: UserId,
    value: Boolean,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserReplaceInfo(
    uid: UserId,
    value: JDoc,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserAddFollowing(
    uid: UserId,
    value: UserId,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserAddFollower(
    uid: UserId,
    value: UserId,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserAddBlocked(
    uid: UserId,
    value: UserId,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserAddTopic(
    uid: UserId,
    value: TopicId,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserAddTag(
    uid: UserId,
    value: String,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserRemoveFollowing(
    uid: UserId,
    value: UserId,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserRemoveFollower(
    uid: UserId,
    value: UserId,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserRemoveBlocked(
    uid: UserId,
    value: UserId,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserRemoveTopic(
    uid: UserId,
    value: TopicId,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class UserRemoveTag(
    uid: UserId,
    value: String,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class TopicReplaceTitle(
    cid: UserId,
    tid: TopicId,
    value: String,
    id: Option[ObjectId] = None,
    parent: Option[ObjectId] = None)
  extends Event

case class TopicReplacePublic(
  cid: UserId,
  tid: TopicId,
  value: Boolean,
  id: Option[ObjectId] = None,
  parent: Option[ObjectId] = None)
  extends Event

case class TopicReplaceInfo(
  cid: UserId,
  tid: TopicId,
  value: JDoc,
  id: Option[ObjectId] = None,
  parent: Option[ObjectId] = None)
  extends Event

case class TopicAddSop(
  cid: UserId,
  tid: TopicId,
  value: UserId,
  id: Option[ObjectId] = None,
  parent: Option[ObjectId] = None)
  extends Event

case class TopicAddParticipant(
  cid: UserId,
  tid: TopicId,
  value: UserId,
  id: Option[ObjectId] = None,
  parent: Option[ObjectId] = None)
  extends Event
*/