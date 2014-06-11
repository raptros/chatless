package chatless.services

import argonaut._
import Argonaut._
import scalaz._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._

trait ErrorReport[R] {
  def operation: String
  def details: Map[String, Json]
  def reason: R

  override def toString = s"ErrorReport(operation = $operation, details = $details, reason = $reason)"
}

object ErrorReport {
  implicit def ErrorReportDecodeJson[R: DecodeJson] = DecodeJson[ErrorReport[R]] { cursor =>
    for {
      o <- cursor.get[String]("operation")
      r <- cursor.get[R]("reason")
      fs <-  (cursor --\ "operation" deleteGoField "reason").deleteGoParent.as[Map[String, Json]]
    } yield new ErrorReport[R] {
      val operation = o
      val details = fs
      val reason: R = r
    }
  }
}
