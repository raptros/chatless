package chatless.db.mongo.builders

import com.osinka.subset._
import chatless.model._
import org.joda.time.DateTime
import argonaut._
import Argonaut._
import chatless.db.mongo.Fields

trait MessageWriter { this: BasicWritables with CoordinateWriter =>
  val messageWritesToDBO = WritesToDBO[Message] { m =>
    val buffer = DBO2(
      Fields._id --> (m.server + m.user + m.topic + m.id),
      Fields.server --> m.server,
      Fields.user --> m.user,
      Fields.topic --> m.topic,
      Fields.id --> m.id,
      Fields.timestamp --> m.timestamp)
    m match {
      case message: PostedMessage => buffer.attach(Fields.poster --> message.poster, Fields.body --> message.body)
      case message: BannerChangedMessage => buffer.attach(Fields.poster --> message.poster, Fields.banner --> message.banner)
      case message: UserJoinedMessage => buffer.attach(Fields.joined --> message.joined)
    }
  }
}
