package chatless.model

import argonaut._
import Argonaut._

sealed trait Coordinate {
  def toList: List[String]
}

case class ServerCoordinate(server: String) extends Coordinate{
  lazy val toList: List[String] = List(server)

  def user(id: String) = UserCoordinate(server, id)
}

object ServerCoordinate {
  implicit def encodeJson: EncodeJson[ServerCoordinate] = jencode1(_.toList)
  implicit def decodeJson: DecodeJson[ServerCoordinate] = jdecode1(ServerCoordinate.apply)
}

case class UserCoordinate(server: String, user: String) extends Coordinate{
  lazy val toList: List[String] = List(server, user)

  def topic(id: String) = TopicCoordinate(server, user, id)
}

object UserCoordinate {
  implicit def encodeJson: EncodeJson[UserCoordinate] = jencode1(_.toList)
  implicit def decodeJson: DecodeJson[UserCoordinate] = jdecode2(UserCoordinate.apply)
}

case class TopicCoordinate(server: String, user: String, topic: String) extends Coordinate{
  lazy val toList: List[String] = List(server, user, topic)
}

object TopicCoordinate {
  implicit def encodeJson: EncodeJson[TopicCoordinate] = jencode1(_.toList)
  implicit def decodeJson: DecodeJson[TopicCoordinate] = jdecode3(TopicCoordinate.apply)
}
