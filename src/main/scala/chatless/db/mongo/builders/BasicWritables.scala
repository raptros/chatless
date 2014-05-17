package chatless.db.mongo.builders

import com.osinka.subset._
import chatless.model._
import org.joda.time.DateTime
import argonaut._
import Argonaut._

trait BasicWritables {
  implicit val jodaDateTime = Field[DateTime] {
    case dt: DateTime => dt
  }

  implicit val datetimeWritable: BsonWritable[DateTime] = BsonWritable[DateTime](identity)

  implicit val jsonAsBson: BsonWritable[Json] = BsonWritable[Json] { j => j.asJson.nospaces }
}
