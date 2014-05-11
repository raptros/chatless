package chatless.db.mongo.parsers
import chatless.model._
import com.osinka.subset._
import DocParser._

trait CoordinateParsers { this: SomeFields =>
  val serverCoordinateParser: DocParser[ServerCoordinate] = server map { ServerCoordinate.apply }

  val userCoordinateParser: DocParser[UserCoordinate] = server ~ user map {
    case s ~ u => UserCoordinate(s, u)
  }

  val topicCoordinateParser: DocParser[TopicCoordinate] = server ~ user ~ topic map {
    case s ~ u ~ t => TopicCoordinate(s, u, t)
  }

  val messageCoordinateParser: DocParser[MessageCoordinate] = server ~ user ~ topic ~ message map {
    case s ~ u ~ t ~ m => MessageCoordinate(s, u, t, m)
  }

  val coordinateParser: DocParser[Coordinate] = {
    messageCoordinateParser | topicCoordinateParser | userCoordinateParser | serverCoordinateParser
  }

  implicit val coordinateField: Field[Coordinate] = Field.fromParser(coordinateParser)
  implicit val userCoordinateField = Field.fromParser(userCoordinateParser)

  val poster = get[UserCoordinate]("poster")
}
