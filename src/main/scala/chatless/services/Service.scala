package chatless.services

import spray.routing._
import spray.http._

import argonaut._

import spray.util.LoggingContext
import chatless.db._
import chatless.operation._

/** defines the chatless service */
trait Service extends MeApi with TopicApi with UserApi with EventApi with TaggedApi {
  /** Route entry point. */
  val chatlessApi = path(PathEnd) {
    get {
      complete("yo")
    }
  }

}
