package chatless.model

import argonaut._
import Argonaut._

sealed trait Coordinate {
  def idPart: String
  def parent: Coordinate
}

case object RootCoordinate extends Coordinate {
  lazy val idPart = ""
  val parent = RootCoordinate
}

case class ServerCoordinate(server: String) extends Coordinate{
  val idPart = server
  lazy val parent = RootCoordinate

  def user(id: String) = UserCoordinate(server, id)
}

object ServerCoordinate {
  implicit def codecJson = casecodec1(ServerCoordinate.apply, ServerCoordinate.unapply)("server")
}

case class UserCoordinate(server: String, user: String) extends Coordinate{
  val idPart = user
  lazy val parent = ServerCoordinate(server)

  def topic(id: String) = TopicCoordinate(server, user, id)
}

object UserCoordinate {
  implicit def codecJson = casecodec2(UserCoordinate.apply, UserCoordinate.unapply)("server", "user")
}

case class TopicCoordinate(server: String, user: String, topic: String) extends Coordinate{
  val idPart = topic
  lazy val parent = UserCoordinate(server, user)

}

object TopicCoordinate {
  implicit def codecJson = casecodec3(TopicCoordinate.apply, TopicCoordinate.unapply)("server", "user", "topic")
}
