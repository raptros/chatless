package chatless.services

import spray.http._
import spray.http.MediaTypes._
import chatless.model._
import spray.httpx.marshalling.{ToResponseMarshaller, Marshaller}
import argonaut._
import Argonaut._
import chatless.db._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import chatless.ops._
import spray.http.HttpHeaders.Location
import chatless.ops.Created

object MarshallingImplicits {
  val debug = true

  implicit def DbErrorReporter = UnifiedErrorReporter[DbError] {
    case NoSuchObject(c) =>
      new CoordinateProblem(StatusCodes.NotFound, "MISSING", c)
    case IdAlreadyUsed(coordinate) =>
      new CoordinateProblem(StatusCodes.BadRequest, "ALREADY_USED", coordinate)
    case MissingCounter(purpose, coordinate) =>
      new CoordinateProblem(StatusCodes.InternalServerError, "COUNTER_MISSING", coordinate, purpose.some)
    case GenerateIdFailed(what, parent, attempted) =>
      new IdGenerationProblem(what, parent, attempted)
    case WriteFailure(what, t) =>
      new DbOpFailure("WRITE_FAILED", what, None, debug option t)
    case WriteFailureWithCoordinate(what, coordinate, t) =>
      new DbOpFailure("WRITE_FAILED", what, coordinate.some, debug option t)
    case ReadFailure(what, t) =>
      new DbOpFailure("READ_FAILED", what, None, debug option t)
    case ReadFailureWithCoordinate(what, coordinate, t) =>
      new DbOpFailure("READ_FAILED", what, coordinate.some, debug option t)
    case DecodeFailure(what, coordinate, causes) =>
      new DbOpListedFailure("DECODE_FAILED", what, coordinate.some, causes)
  }

  implicit def OperationFailureReporter: UnifiedErrorReporter[OperationFailure] = UnifiedErrorReporter[OperationFailure] {
    case AddMemberFailed(topic, user, cause) =>
      new DbCausedOperationFailure("ADD_MEMBER_FAILED", cause, "topic" := topic, "user" := user)
    case SendInviteFailed(topic, user, cause) =>
      new ErrorReportFromCause("SEND_INVITE_FAILED", cause, "topic" := topic, "user" := user)
    case CreateTopicFailed(user, init, cause) =>
      new DbCausedOperationFailure("CREATE_TOPIC_FAILED", cause, "user" := user)
    case SetFirstMemberFailed(topic, cause) =>
      new DbCausedOperationFailure("SET_FIRST_MEMBER_FAILED", cause, "topic" := topic)
    case UserNotLocal(user, server) =>
      new GenericErrorReport(StatusCodes.InternalServerError, "USER_NOT_LOCAL", "user" := user, "server" := server)
  }

  implicit def unifiedErrorResponseMarshaller[A](implicit reporter: UnifiedErrorReporter[A]): ToResponseMarshaller[A] =
    ToResponseMarshaller.delegate[A, UnifiedErrorReport](`application/json`) { err: A => reporter(err) }

  import Uri.Path

  def coordinatePath(c: Coordinate): Path = (c.walk.reverse flatMap { Coordinate.idAndName } foldLeft (Path.Empty: Path)) {
    case (p, (n, id)) => p / n / id
  }

  type TripleResponse[A] = (StatusCode, Seq[HttpHeader], A)
  implicit def createdResponseMarshaller[A <: HasCoordinate[Coordinate]: Marshaller]: ToResponseMarshaller[Created[A]] =
    ToResponseMarshaller.delegate[Created[A], TripleResponse[A]](`application/json`) { created: Created[A] =>
      (StatusCodes.Created,
        Location(Uri(path = coordinatePath(created.coordinate))) :: Nil,
        created.a)
    }
}
