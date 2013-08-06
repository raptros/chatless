package chatless.services

import spray.routing._
import spray.http._

import argonaut._

import spray.util.LoggingContext
import chatless.db._

import scalaz.std.function._
import scalaz.syntax.semigroup._

/** defines the chatless service */
trait Service extends MeApi with TopicApi with UserApi with EventApi with TaggedApi {
  def allapis = (meApi _) |+| (userApi _)

  /** Route entry point. */
  val chatlessApi = path(PathEnd) {
    get {
      complete("yo")
    }
  }

}
