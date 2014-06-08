package chatless.services

import spray.http.{StatusCodes, StatusCode}
import argonaut._
import Argonaut._
import spray.httpx.marshalling.ToResponseMarshaller
import chatless.model.{TopicCoordinate, UserCoordinate, Coordinate}
import spray.http.MediaTypes._
import chatless.db.DbError
import chatless.model.topic.{MemberMode, TopicMode}

trait UnifiedErrorReport {
  type R

  def errorCode: StatusCode

  def operation: String

  def reason: R

  implicit def encodeReason: EncodeJson[R]

  def details: Seq[(String, Json)]

  lazy val json: Json = ("operation" := operation) ->: ("reason" := reason) ->: jObjectAssocList(details.toList)

  def asResponse = errorCode -> json
}

object UnifiedErrorReport {
  implicit def encodeJson = EncodeJson[UnifiedErrorReport] { report => report.json }

  implicit def toResponse: ToResponseMarshaller[UnifiedErrorReport] =
    ToResponseMarshaller.delegate[UnifiedErrorReport, (StatusCode, UnifiedErrorReport)](`application/json`) { report: UnifiedErrorReport =>
      report.errorCode -> report
    }
}

class SimpleErrorReport(
    val errorCode: StatusCode,
    val operation: String,
    val reason: String,
    val details: (String, Json)*)
  extends UnifiedErrorReport {
  type R = String
  implicit val encodeReason = StringEncodeJson
}

class ExceptionErrorReport(
    val operation: String,
    val exception: Option[Throwable],
    ds: (String, Json)*)
  extends UnifiedErrorReport {

  val errorCode = StatusCodes.InternalServerError

  type R = String
  val reason = "EXCEPTION"
  implicit val encodeReason = StringEncodeJson

  lazy val details: Seq[(String, Json)] = (exception fold ds) { e => ("exception" := e) +: ds }
}

class MultiCauseErrorReport(
    val operation: String,
    val reason: List[String],
    val details: (String, Json)*)
  extends UnifiedErrorReport {

  type R = List[String]
  implicit def encodeReason = ListEncodeJson[String]

  def errorCode: StatusCode = StatusCodes.InternalServerError
}

class ErrorReportFromCause[E](
    val operation: String,
    val cause: E,
    val details: (String, Json)*)
    (implicit reporter: UnifiedErrorReporter[E])
  extends UnifiedErrorReport {

  type R = UnifiedErrorReport
  lazy val reason = reporter(cause)
  implicit def encodeReason = UnifiedErrorReport.encodeJson

  lazy val errorCode = reason.errorCode

  lazy val info = jObjectFields(details: _*)
}


