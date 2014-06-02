package chatless.model

import argonaut._
import Argonaut._
import chatless.macros.JsonMacros
import scalaz._
import ids._

sealed trait Coordinate {
  type IDTag
  def id: String @@ IDTag
  def parent: Coordinate

  /** returns each coordinate starting with this one back up to RootCoordinate.
    * this is safe as long as there are no loops in the coordinate heirarchy, which there should never be.
    * as long as every coordinate below the root has strictly more parameters than that coordinate's parent, it is fine.
    */
  def walk: List[Coordinate] = this :: { parent.walk }
}

case object RootCoordinate extends Coordinate {
  type IDTag = RootId
  lazy val id = RootId("")
  val parent = RootCoordinate

  override def walk: List[Coordinate] = Nil
}

case class ServerCoordinate(server: String @@ ServerId) extends Coordinate{
  type IDTag = ServerId
  val id = server
  lazy val parent = RootCoordinate

  def user(id: String @@ UserId) = UserCoordinate(server, id)
}

object ServerCoordinate {
  implicit def codecJson = JsonMacros.deriveCaseCodecJson[ServerCoordinate]
}

case class UserCoordinate(server: String @@ ServerId, user: String @@ UserId) extends Coordinate{
  type IDTag = UserId
  val id = user
  lazy val parent = ServerCoordinate(server)

  def topic(id: String @@ TopicId) = TopicCoordinate(server, user, id)
}

object UserCoordinate {
  implicit def codecJson = JsonMacros.deriveCaseCodecJson[UserCoordinate]
}

case class TopicCoordinate(server: String @@ ServerId, user: String @@ UserId, topic: String @@ TopicId) extends Coordinate{
  type IDTag = TopicId
  val id = topic
  lazy val parent = UserCoordinate(server, user)

  def message(id: String @@ MessageId) = MessageCoordinate(server, user, topic, id)
}

object TopicCoordinate {
  implicit def codecJson = JsonMacros.deriveCaseCodecJson[TopicCoordinate]
}

case class MessageCoordinate(server: String @@ ServerId, user: String @@ UserId, topic: String @@ TopicId, message: String @@ MessageId) extends Coordinate {
  type IDTag = MessageId
  val id = message
  lazy val parent = TopicCoordinate(server, user, topic)
}

object MessageCoordinate {
  implicit def codecJson = JsonMacros.deriveCaseCodecJson[MessageCoordinate]
}

object Coordinate {
  implicit def CoordinateEncodeJson = EncodeJson[Coordinate] {
    case RootCoordinate => jEmptyObject
    case c: ServerCoordinate => c.asJson
    case c: UserCoordinate => c.asJson
    case c: TopicCoordinate => c.asJson
    case c: MessageCoordinate => c.asJson
  }

  def name(c: Coordinate): Option[String] = c match {
    case RootCoordinate => None
    case _: ServerCoordinate => Some("server")
    case _: UserCoordinate => Some("user")
    case _: TopicCoordinate => Some("topic")
    case _: MessageCoordinate => Some("message")
  }

  def idAndName(c: Coordinate): Option[(String, String)] = name(c) map { c.id.asInstanceOf[String].-> }
}

