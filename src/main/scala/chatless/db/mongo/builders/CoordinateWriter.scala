package chatless.db.mongo.builders

import com.osinka.subset._
import chatless.model._

import argonaut._
import Argonaut._
import chatless.db.mongo.Fields

trait CoordinateWriter {
  def buildDBOForCoordinate(c: Coordinate): DBObjectBuffer = c match {
    case RootCoordinate => DBO()
    case ServerCoordinate(server) =>
      DBO2(Fields.server --> server)
    case UserCoordinate(server, user) =>
      DBO2(Fields.server --> server, Fields.user --> user)
    case TopicCoordinate(server, user, topic) =>
      DBO2(Fields.server --> server, Fields.user --> user, Fields.topic --> topic)
    case MessageCoordinate(server, user, topic, message) =>
      DBO2(Fields.server --> server, Fields.user --> user, Fields.topic --> topic, Fields.message --> message)
  }

  implicit val coordinateAsBson = BsonWritable[Coordinate] { c => buildDBOForCoordinate(c)() }

  val coordinateWritesToDBO = WritesToDBO[Coordinate] { buildDBOForCoordinate }
}
