package chatless.services
import chatless._

import spray.routing._
import spray.http._
import MediaTypes._

import argonaut._
import Argonaut._

import spray.util.LoggingContext
import chatless.db._

import scalaz.std.function._
import scalaz.syntax.semigroup._
import scalaz.Semigroup
import Semigroup._

/** defines the chatless service */
trait Service extends ServiceBase {

  val userApi: CallerRoute = new UserApi(dbac)
  val meApi: CallerRoute = new MeApi(dbac)
  val topicApi: CallerRoute = new TopicApi(dbac)
  val eventApi: CallerRoute = new EventApi(dbac)
  val taggedApi: CallerRoute = new TaggedApi(dbac)

  def allApis:CallerRoute = meApi |+| userApi |+| topicApi |+| eventApi |+| taggedApi


  def completeStateError(err: StateError, code: StatusCode) = respondWithMediaType(`application/json`) {
    complete { code -> err.asJson.spaces2 }
  }

  def handleStateError(se: StateError) = se match {
    case e: TopicNotFoundError    => completeStateError(e, StatusCodes.NotFound)
    case e: OperationNotSupported => completeStateError(e, StatusCodes.MethodNotAllowed)
    case e: UnhandleableMessage   => completeStateError(e, StatusCodes.InternalServerError)
    case e: AccessNotPermitted => completeStateError(e, StatusCodes.Forbidden)
    case e: NonExistentField => completeStateError(e, StatusCodes.NotFound)
  }

  implicit def serviceHandler(implicit log: LoggingContext) = ExceptionHandler {
    case (se: StateError) => log.warning(se.getMessage); handleStateError(se)
    case (t: Throwable) => log.warning(t.getMessage); respondWithMediaType(`application/json`) {
      complete { StatusCodes.InternalServerError -> ("failed".asJson -->>: jEmptyArray).nospaces }
    }
  }

  /** Route entry point. */
  val chatlessApi = path(PathEnd) {
    get {
      complete("yo")
    }
  }

}
