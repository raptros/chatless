package chatless.db.mongo.codecs

import io.github.raptros.bson._
import Bson._
import chatless.model._
import com.mongodb.DBObject

trait CoordinateCodec {
  implicit def ServerCoordinateDecodeBson: DecodeBson[ServerCoordinate] =
    BsonMacros.deriveCaseDecodeBson[ServerCoordinate]

  implicit def UserCoordinateDecodeBson: DecodeBson[UserCoordinate] =
    BsonMacros.deriveCaseDecodeBson[UserCoordinate]

  implicit def TopicCoordinateDecodeBson: DecodeBson[TopicCoordinate] =
    BsonMacros.deriveCaseDecodeBson[TopicCoordinate]

  implicit def MessageCoordinateDecodeBson: DecodeBson[MessageCoordinate] =
    BsonMacros.deriveCaseDecodeBson[MessageCoordinate]

  //remember to keep these in decreasing-arity order
  implicit def CoordinateDecodeBson: DecodeBson[Coordinate] =
    MessageCoordinateDecodeBson |||
      TopicCoordinateDecodeBson |||
      UserCoordinateDecodeBson |||
      ServerCoordinateDecodeBson

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
