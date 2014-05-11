package chatless.db.mongo.parsers

import com.osinka.subset._
import chatless.model._

import argonaut._
import Argonaut._
import com.mongodb.casbah.Imports._
import org.joda.time.DateTime
import java.util.Date

trait SomeFields { this: CoordinateParsers =>
  implicit val jodaDateTime = Field[DateTime] {
    case dt: DateTime => dt
  }

  implicit val jsonField = new Field[Json] {
    override def apply(o: Any): Option[Json] = o match {
      case s: String => s.parseOption
    }
  }

  implicit val userCoordinateField = Field.fromParser(userCoordinateParser)

  import DocParser._

  //some fields
  val server = str("server")
  val user = str("user")
  val topic = str("topic")
  val message = str("message")
  val id = str("id")
  val timestamp = get[DateTime]("timestamp")
  val poster = get[UserCoordinate]("poster")

}
