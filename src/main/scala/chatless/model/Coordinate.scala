package chatless.model

import argonaut._
import Argonaut._

sealed trait Coordinate {
  def id: String
  def parent: Coordinate
}

case object RootCoordinate extends Coordinate {
  lazy val id = ""
  val parent = RootCoordinate
}

case class ServerCoordinate(server: String) extends Coordinate{
  val id = server
  lazy val parent = RootCoordinate

  def user(id: String) = UserCoordinate(server, id)
}

object ServerCoordinate {
  implicit def codecJson = casecodec1(ServerCoordinate.apply, ServerCoordinate.unapply)("server")
}

case class UserCoordinate(server: String, user: String) extends Coordinate{
  val id = user
  lazy val parent = ServerCoordinate(server)

  def topic(id: String) = TopicCoordinate(server, user, id)
}

object UserCoordinate {
  implicit def codecJson = casecodec2(UserCoordinate.apply, UserCoordinate.unapply)("server", "user")
}

case class TopicCoordinate(server: String, user: String, topic: String) extends Coordinate{
  val id = topic
  lazy val parent = UserCoordinate(server, user)

  def message(id: String) = MessageCoordinate(server, user, topic, id)
}

object TopicCoordinate {
  implicit def codecJson = casecodec3(TopicCoordinate.apply, TopicCoordinate.unapply)("server", "user", "topic")
}

case class MessageCoordinate(server: String, user: String, topic: String, message: String) extends Coordinate {
  val id = message
  lazy val parent = TopicCoordinate(server, user, topic)
}

object MessageCoordinate {
  implicit def codecJson =
    casecodec4(MessageCoordinate.apply, MessageCoordinate.unapply)("server", "user", "topic", "message")
}
