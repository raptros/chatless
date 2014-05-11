package chatless.db.mongo

import com.osinka.subset._
import chatless.model._

import BuilderUtils._
import chatless.model.UserJoinedMessage
import chatless.model.PostedMessage
import chatless.model.BannerChangedMessage
import argonaut._
import Argonaut._
import com.mongodb.casbah.Imports._
import org.joda.time.DateTime
import java.util.Date
import scala.annotation.implicitNotFound

object Serializers {
  import com.mongodb.casbah.commons.conversions.scala._
  RegisterJodaTimeConversionHelpers()

  implicit val jodaDateTime = Field[DateTime] {
    case dt: DateTime => dt
  }

  implicit val datetimeWritable = BsonWritable[DateTime](identity)

  def buildDBOForCoordinate(c: Coordinate): DBObjectBuffer = c match {
    case RootCoordinate => DBO()
    case ServerCoordinate(server) =>
      DBO2(Fields.server -> server)
    case UserCoordinate(server, user) =>
      DBO2(Fields.server -> server, Fields.user -> user)
    case TopicCoordinate(server, user, topic) =>
      DBO2(Fields.server -> server, Fields.user -> user, Fields.topic -> topic)
    case MessageCoordinate(server, user, topic, message) =>
      DBO2(Fields.server -> server, Fields.user -> user, Fields.topic -> topic, Fields.message -> message)
  }

  implicit val CoordinateAsBson: BsonWritable[Coordinate] = BsonWritable[Coordinate] { c => buildDBOForCoordinate(c)() }


  def coordinateQuery(coord: Coordinate): DBObject =
    buildDBOForCoordinate(coord.parent).attach(Fields.id -> coord.idPart).apply()

  implicit val JsonAsBson = BsonWritable[Json] { j => j.asJson.nospaces }

  @implicitNotFound("could not fine a WritesToDBO instance for ${A}")
  trait WritesToDBO[-A] {
    def write(a: A): DBObjectBuffer
  }

  object WritesToDBO {
    def apply[A](f: A => DBObjectBuffer): WritesToDBO[A] = new WritesToDBO[A] {
      def write(a: A): DBObjectBuffer = f(a)
    }
  }

  implicit val coordinateWritesToDBO = WritesToDBO[Coordinate] { buildDBOForCoordinate }

  implicit val messageWritesToDBO = WritesToDBO[Message] { m =>
    val buffer = DBO2(
      Fields._id -> (m.server + m.user + m.topic + m.id),
      Fields.server -> m.server,
      Fields.user -> m.user,
      Fields.topic -> m.topic,
      Fields.id -> m.id,
      Fields.timestamp -> m.timestamp)
    m match {
      case message: PostedMessage => buffer.attach(Fields.poster -> message.poster, Fields.body -> message.body)
      case message: BannerChangedMessage => buffer.attach(Fields.poster -> message.poster, Fields.banner -> message.banner)
      case message: UserJoinedMessage => buffer.attach(Fields.joined -> message.joined)
    }
  }

  implicit val topicWritesToDBO = WritesToDBO[Topic] { topic =>
    DBO2(
      //ensure that _id collision has exactly the same behavior as coordinate collision
      Fields._id -> (topic.server + topic.user + topic.id),
      Fields.server -> topic.server,
      Fields.user -> topic.user,
      Fields.id -> topic.id,
      Fields.banner -> topic.banner,
      Fields.info -> topic.info.asJson.nospaces
    )
  }


  implicit class GetDBOFor[A](a: A)(implicit w: WritesToDBO[A]) {
    def getDBO: DBObject = w.write(a)()
  }
}
