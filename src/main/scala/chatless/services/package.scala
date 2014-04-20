package chatless

import scalaz.Semigroup
import spray.routing.Route
import spray.routing.RouteConcatenation._
import org.joda.time.DateTime
import spray.http.{MediaType, ContentTypeRange, HttpEntity, StatusCode}
import shapeless.HList

import scalaz.syntax.id._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import scalaz.std.option._

import spray.http.MediaTypes._
import chatless.model.{Coordinate, TopicCoordinate, User}
import spray.httpx.marshalling.Marshaller
import argonaut._
import Argonaut._
import spray.httpx.unmarshalling.{ContentExpected, SimpleUnmarshaller, MalformedContent, Unmarshaller}

package object services {

  type CallerRoute = UserId => Route

  implicit def routeSemigroup: Semigroup[Route] = new Semigroup[Route] {
    def append(r1: Route, r2: => Route) = r1 ~ r2
  }



/*  private def stackTrace2StringList(st: Array[StackTraceElement]) = {
    val stList = (st ?? Array()).toList map { _.toString }
    stList.nonEmpty ?? stList.some
  }

  /** a tidy way to produce a json representation of any Throwable. */
  def throwableToJson(t: Throwable): JObject = JObject() ~
    ("type"    -> t.getClass.toString) ~
    ("message" -> Option(t.getMessage)) ~
    ("trace"   -> stackTrace2StringList(t.getStackTrace)) ~
    ("cause"   -> (Option(t.getCause) map { throwableToJson }))
*/
  val ME_API_BASE = "me"
  val TOPIC_API_BASE = "topic"
  val USER_API_BASE = "user"
  val MESSAGE_API_BASE = "message"
  val TAGGED_API_BASE = "tagged"
  val EVENT_API_BASE = "events"

  def Header(name: String): String = s"x-chatless-$name"

  val X_UPDATED = Header("updated")
  val X_CREATED_TOPIC = Header("created-topic")
  val X_CREATED_MESSAGE = Header("created-message")

  implicit val jsonMarshaller = Marshaller.delegate[Json, String](`application/json`) { (j: Json) => j.nospaces }

  implicit def delegateToJson[A: EncodeJson] = Marshaller.delegate[A, Json](`application/json`) { (a: A) => a.asJson }

//  implicit val userMarshaller = delegateToJson[User]

  implicit def listMarshaller[A: EncodeJson] = Marshaller.delegate[List[A], Json](`application/json`) {
    l: List[A] => l.asJson
  }

  implicit def jsonUnmarshaller: Unmarshaller[Json] = new SimpleUnmarshaller[Json] {
    def unmarshal(entity: HttpEntity) = entity match {
      case HttpEntity.NonEmpty(ctype, data) => Parse.parse(data.asString).toEither.left map { MalformedContent(_: String) }
      case _ => Left(ContentExpected)
    }

    val canUnmarshalFrom: Seq[ContentTypeRange] = `application/json` :: Nil
  }
}
