package chatless.services

import spray.routing.Route
import spray.routing.RouteConcatenation._
import org.joda.time.DateTime
import spray.http._
import shapeless.HList

import scalaz.syntax.id._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import scalaz.std.option._

import spray.http.MediaTypes._
import chatless.model._
import spray.httpx.marshalling.{ToResponseMarshaller, Marshaller}
import argonaut._
import Argonaut._
import spray.httpx.unmarshalling.{ContentExpected, SimpleUnmarshaller, MalformedContent, Unmarshaller}
import chatless.db._
import chatless.db.GenerateIdFailed
import chatless.db.IdAlreadyUsed
import chatless.db.WriteFailure
import chatless.db.GenerateIdFailed
import chatless.db.DeserializationErrors
import chatless.db.IdAlreadyUsed
import chatless.db.WriteFailure
import scalaz.\/

object MarshallingImplicits {
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

  implicit def delegateFromJson[A: DecodeJson] = Unmarshaller.delegate[Json, A](`application/json`) { j: Json =>
    j.as[A].fold((m, h) => throw new IllegalArgumentException(m), identity)
  }

  implicit def CoordinateEncodeJson = EncodeJson[Coordinate] {
    case RootCoordinate => jEmptyObject
    case c: ServerCoordinate => c.asJson
    case c: UserCoordinate => c.asJson
    case c: TopicCoordinate => c.asJson
    case c: MessageCoordinate => c.asJson
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

  object Errors extends Enumeration {
    type Error = Value
    val ID_ALREADY_USED,
    GENERATE_ID_FAILED,
    READ_FAILURE,
    WRITE_FAILURE,
    MISSING_COUNTER,
    DESERIALIZATION_ERRORS = Value
  }
  import Errors._
  def x_chatless_error(value: Error): HttpHeader = HttpHeaders.RawHeader("X-Chatless-Error", value.toString)
  def x_chatless_errors(errors: Error*): Seq[HttpHeader] = errors map { x_chatless_error }

  def produceResponseForError(err: DbError): (StatusCode, Seq[HttpHeader], Json) = err match {
    case IdAlreadyUsed(coord) => (
      StatusCodes.BadRequest,
      x_chatless_errors(ID_ALREADY_USED),
      coord.asJson
      )
    case GenerateIdFailed(what, parent, attempted) => (
      StatusCodes.InternalServerError,
      x_chatless_errors(GENERATE_ID_FAILED),
      ("parent" := parent) ->: ("attempted" := attempted) ->: jEmptyObject
      )
    case WriteFailure(what, t) => (
      StatusCodes.InternalServerError,
      x_chatless_errors(WRITE_FAILURE),
      ("what" := what) ->: ("t" := t) ->: jEmptyObject
      )
    case WriteFailureWithCoordinate(what, coordinate, t) => (
      StatusCodes.InternalServerError,
      x_chatless_errors(WRITE_FAILURE),
      ("what" := what) ->: ("coordinate" := coordinate) ->: ("t" := t) ->: jEmptyObject
      )
    case DeserializationErrors(messages) => (
      StatusCodes.InternalServerError,
      x_chatless_errors(DESERIALIZATION_ERRORS),
      messages.asJson
      )
    case MissingCounter(purpose, coordinate) => (
      StatusCodes.InternalServerError,
      x_chatless_errors(MISSING_COUNTER),
      ("purpose" := purpose) ->: ("coordinate" := coordinate) ->: jEmptyObject
      )
    case NoSuchObject(c) => (StatusCodes.NotFound, Nil, c.asJson)
    case ReadFailure(what, t) => (
      StatusCodes.InternalServerError,
      x_chatless_errors(READ_FAILURE),
      ("what" := what) ->: ("t" := t) ->: jEmptyObject
      )
    case ReadFailureWithCoordinate(what, coordinate, t) => (
      StatusCodes.InternalServerError,
      x_chatless_errors(READ_FAILURE),
      ("what" := what) ->: ("coordinate" := coordinate) ->: ("t" := t) ->: jEmptyObject
      )
  }

  implicit def dbErrorResponseMarshaller: ToResponseMarshaller[DbError] = 
    ToResponseMarshaller.delegate[DbError, (StatusCode, Seq[HttpHeader], Json)](`application/json`) {
      produceResponseForError _
    }

  implicit def scalazEitherToResMarshaller[A, B](implicit ma: ToResponseMarshaller[A], mb: ToResponseMarshaller[B]) =
    ToResponseMarshaller[A \/ B] { (value, ctx) =>
      value.fold(ma(_, ctx), mb(_, ctx))
    }


}
