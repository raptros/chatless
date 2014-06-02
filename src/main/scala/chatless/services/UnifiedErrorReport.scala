package chatless.services

import spray.http.{StatusCodes, StatusCode}
import argonaut._
import Argonaut._
import spray.httpx.marshalling.ToResponseMarshaller
import chatless.model.Coordinate
import spray.http.MediaTypes._
import chatless.db.DbError

trait UnifiedErrorReport {

  def errorCode: StatusCode

  def description: String

  def info: Json

  lazy val json: Json = ("description" := description) ->: info

  def asResponse = errorCode -> json
}

object UnifiedErrorReport {
  implicit def encodeJson = EncodeJson[UnifiedErrorReport] { report => report.json }

  implicit def toResponse: ToResponseMarshaller[UnifiedErrorReport] =
    ToResponseMarshaller.delegate[UnifiedErrorReport, (StatusCode, UnifiedErrorReport)](`application/json`) { report: UnifiedErrorReport =>
      report.errorCode -> report
    }
}

trait UnifiedErrorReportWithCause extends UnifiedErrorReport {
  def cause: UnifiedErrorReport

  override lazy val json = ("cause" := cause) ->: ("description" := description) ->: info
}

class CoordinateProblem(
    val errorCode: StatusCode,
    val description: String,
    coordinate: Coordinate,
    purpose: Option[String] = None)
  extends UnifiedErrorReport {

  lazy val info =
    ("coordinate" := coordinate) ->:
      ("purpose" :=? purpose) ->?:
      jEmptyObject

}

class IdGenerationProblem(
    what: String,
    parent: Coordinate,
    attempted: List[String])
  extends UnifiedErrorReport {

  val errorCode = StatusCodes.InternalServerError

  val description = "GENERATE_ID_FAILED"

  lazy val info = ("what" := what) ->:
    ("parent" := parent) ->:
    ("attempted" := attempted) ->:
    jEmptyObject
}

class DbOpFailure(
    val description: String,
    what: String,
    coordinate: Option[Coordinate],
    t: Option[Throwable])
  extends UnifiedErrorReport {

  val errorCode = StatusCodes.InternalServerError

  lazy val info = ("what" := what) ->:
    ("coordinate" :=? coordinate) ->?:
    ("cause" :=? t) ->?:
    jEmptyObject
}

class DbOpListedFailure[A: EncodeJson](
    val description: String,
    what: String,
    coordinate: Option[Coordinate],
    causes: List[A])
  extends UnifiedErrorReport {

  val errorCode = StatusCodes.InternalServerError

  val info = ("what" := what) ->:
    ("coordinate" :=? coordinate) ->?:
    ("causes" := causes) ->:
    jEmptyObject
}


class DbCausedOperationFailure(
    val description: String,
    val causedBy: DbError,
    extra: (String, Json)*)(
    implicit reporter: UnifiedErrorReporter[DbError])
  extends UnifiedErrorReportWithCause {

  def cause = reporter(causedBy)

  def errorCode = cause.errorCode

  def info = jObjectFields(extra: _*)
}

class GenericErrorReport(
    val errorCode: StatusCode,
    val description: String,
    val fields: (String, Json)*)
  extends UnifiedErrorReport {
  lazy val info = jObjectFields(fields: _*)
}

class ErrorReportFromCause[E](
    val description: String,
    val causedBy: E,
    extra: (String, Json)*)(
    implicit reporter: UnifiedErrorReporter[E])
  extends UnifiedErrorReportWithCause {

  lazy val cause = reporter(causedBy)
  lazy val errorCode = cause.errorCode
  lazy val info = jObjectFields(extra: _*)
}
