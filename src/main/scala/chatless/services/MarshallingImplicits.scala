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
import chatless.ops.Preconditions._
import chatless.model.topic.MemberMode

object MarshallingImplicits {
  val debug = true

  implicit def DbErrorReporter = UnifiedErrorReporter[DbError] {
    case NoSuchObject(c) =>
      new SimpleErrorReport(StatusCodes.NotFound, "GET", "MISSING", "coordinate" := c)
    case IdAlreadyUsed(coordinate) =>
      new SimpleErrorReport(StatusCodes.BadRequest, "CREATE", "CONFLICT", "coordinate" := coordinate)
    case MissingCounter(purpose, coordinate) =>
      new SimpleErrorReport(StatusCodes.InternalServerError, "INC_COUNTER", "MISSING", "coordinate" := coordinate, "purpose" := purpose.some)
    case GenerateIdFailed(what, parent, attempted) =>
      new SimpleErrorReport(StatusCodes.InternalServerError, "GENERATE_ID", "FAILED", "what" := what, "coordinate" := parent, "attempted" := attempted)
    case WriteFailure(what, t) =>
      new ExceptionErrorReport("WRITE", debug option t, "what" := what)
    case WriteFailureWithCoordinate(what, coordinate, t) =>
      new ExceptionErrorReport("WRITE", debug option t, "what" := what, "coordinate" := coordinate)
    case ReadFailure(what, t) =>
      new ExceptionErrorReport("READ", debug option t, "what" := what)
    case ReadFailureWithCoordinate(what, coordinate, t) =>
      new ExceptionErrorReport("READ", debug option t, "what" := what, "coordinate" := coordinate)
    case DecodeFailure(what, coordinate, causes) =>
      new MultiCauseErrorReport("DECODE", causes, "what" := what, "coordinate" := coordinate)
  }

  implicit def OperationFailureReporter: UnifiedErrorReporter[OperationFailure] = UnifiedErrorReporter[OperationFailure] {
    case PreconditionFailed(op, failure, coordinates @ _*) =>
      new SimpleErrorReport(codeForPrecondition(failure), op.toString, failure.toString, coordinates.toSeq map pair2jAssoc: _*)
    case DbOperationFailed(operation, resource, cause) =>
      new ErrorReportFromCause(operation.toString, cause, "resource" := resource)
    case InnerOperationFailed(operation, resource, cause) =>
      new ErrorReportFromCause(operation.toString, cause, "resource" := resource)
  }

  private def pair2jAssoc[A: EncodeJson]: ((String, A)) => (String, Json) = p => p._1 := p._2

  private def codeForPrecondition(pre: Precondition): StatusCode = pre match {
    case READ_DENIED => StatusCodes.Forbidden
    case USER_NOT_LOCAL => StatusCodes.InternalServerError
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
