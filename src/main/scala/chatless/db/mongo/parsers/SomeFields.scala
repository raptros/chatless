package chatless.db.mongo.parsers

import com.osinka.subset._

import argonaut._
import Argonaut._
import org.joda.time.DateTime

trait SomeFields {
  implicit val jodaDateTime = Field[DateTime] {
    case dt: DateTime => dt
  }

  implicit val jsonField = new Field[Json] {
    override def apply(o: Any): Option[Json] = o match {
      case s: String => s.parseOption
    }
  }

  import DocParser._

  //some fields
  val server = str("server")
  val user = str("user")
  val topic = str("topic")
  val message = str("message")
  val id = str("id")
  val timestamp = get[DateTime]("timestamp")

}
