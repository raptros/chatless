package chatless

import scalaz.{\/, Semigroup}
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
import spray.httpx.marshalling.{ToResponseMarshaller, Marshaller}
import argonaut._
import Argonaut._
import spray.httpx.unmarshalling.{ContentExpected, SimpleUnmarshaller, MalformedContent, Unmarshaller}

package object services {

  type CallerRoute = String => Route

  implicit def routeSemigroup: Semigroup[Route] = new Semigroup[Route] {
    def append(r1: Route, r2: => Route) = r1 ~ r2
  }

  val ME_API_BASE = "me"
  val TOPIC_API_BASE = "topic"
  val USER_API_BASE = "user"
  val MESSAGE_API_BASE = "message"
  val TAGGED_API_BASE = "tagged"
  val EVENT_API_BASE = "events"

  implicit val jsonMarshaller = Marshaller.delegate[Json, String](`application/json`) { (j: Json) => j.nospaces }

  implicit def delegateToJson[A: EncodeJson] = Marshaller.delegate[A, Json](`application/json`) { (a: A) => a.asJson }

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

  implicit def delegateFromJson[A: DecodeJson] = Unmarshaller.delegate[Json, A](`application/json`) { j: Json =>
    j.as[A].fold((m, h) => throw new IllegalArgumentException(m), identity)
  }

  implicit def ArrayEncodeJson[A](implicit e: EncodeJson[List[A]]) = e.contramap[Array[A]] { _.toList }

  implicit def stackTraceElementEncodeJson = EncodeJson[StackTraceElement] { ste =>
    ("class" :=? Option(ste.getClassName)) ->?: // according to the docs, class and method should never be null
      ("method" :=? Option(ste.getMethodName)) ->?: //but it doesn't hurt to check, right?
      ("file" :=? Option(ste.getFileName)) ->?:
      ("line" := ste.getLineNumber) ->:
      jEmptyObject
  }

  /** needs explicit type annotation because it recursively encodes throwables. */
  implicit def throwableEncodeJson: EncodeJson[Throwable] = EncodeJson[Throwable] { t =>
    ("type" := t.getClass.toString) ->:
      ("message" :=? Option(t.getMessage)) ->?:
      ("trace" := t.getStackTrace ?? Array()) ->:
      ("cause" :=? Option(t.getCause)) ->?:
      jEmptyObject
  }

  implicit def scalazEitherToResMarshaller[A, B](implicit ma: ToResponseMarshaller[A], mb: ToResponseMarshaller[B]) =
    ToResponseMarshaller[A \/ B] { (value, ctx) =>
      value.fold(ma(_, ctx), mb(_, ctx))
    }

  implicit def dateTimeEncodeJson = EncodeJson[DateTime] { dt => jString(dt.toString) }

  implicit def dateTimeDecodeJson = DecodeJson[DateTime] { c =>
    for {
      ts <- c.as[String]
      dt <- catchJodaParseFailure(c)(DateTime.parse(ts))
    } yield dt
  }

  private def catchJodaParseFailure(c: HCursor)(jOp: => DateTime): DecodeResult[DateTime] = try { okResult(jOp) } catch {
    case e: IllegalArgumentException => failResult(e.getMessage, c.history)
  }

}
