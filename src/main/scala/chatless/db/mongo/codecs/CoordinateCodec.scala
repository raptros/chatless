package chatless.db.mongo.codecs

import io.github.raptros.bson._
import Bson._
import chatless.model._
import com.mongodb.DBObject

trait CoordinateCodec {
  implicit def ServerCoordinateDecodeBson: DecodeBson[ServerCoordinate] =
    bdecode1f(ServerCoordinate.apply)("server")

  implicit def UserCoordinateDecodeBson: DecodeBson[UserCoordinate] =
    bdecode2f(UserCoordinate.apply)("server", "user")

  implicit def TopicCoordinateDecodeBson: DecodeBson[TopicCoordinate] =
    bdecode3f(TopicCoordinate.apply)("server", "user", "topic")

  implicit def MessageCoordinateDecodeBson: DecodeBson[MessageCoordinate] =
    bdecode4f(MessageCoordinate.apply)("server", "user", "topic", "message")

  implicit def CoordinateDecodeBson: DecodeBson[Coordinate] = {
    ServerCoordinateDecodeBson ||| UserCoordinateDecodeBson ||| TopicCoordinateDecodeBson ||| MessageCoordinateDecodeBson
  }

  def getKVForId(c: Coordinate): Option[DBOKV[String]] = c match {
    case RootCoordinate => None
    case _: ServerCoordinate => Some("server" :> c.id)
    case _: UserCoordinate => Some("user" :> c.id)
    case _: TopicCoordinate => Some("topic" :> c.id)
    case _: MessageCoordinate => Some("message" :> c.id)
  }

  def getDBOForCoordinate(c: Coordinate): DBObject = (c.walk flatMap { getKVForId } foldLeft DBO()) { _ +@+ _ }

  implicit def CoordinateEncodeBson: EncodeBson[Coordinate] = EncodeBson[Coordinate] { getDBOForCoordinate }


}
