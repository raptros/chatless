package chatless.services


import spray.routing._
import spray.util.LoggingContext
import spray.http._
import MediaTypes._

import spray.httpx.Json4sSupport

import chatless.responses._
import chatless.services.routeutils.HelperDirectives

trait ServiceBase extends HttpService with Json4sSupport with HelperDirectives {

  def handleStateError(se: StateError) = se match {
    case _: UnhandleableMessageError => se complete StatusCodes.InternalServerError
    case _: UserNotFoundError => se complete StatusCodes.NotFound
    case _: ModelExtractionError => se complete StatusCodes.InternalServerError
    case _: TopicNotFoundError => se complete StatusCodes.NotFound
  }

  implicit def serviceHandler(implicit logC: LoggingContext) = ExceptionHandler {
    case (se: StateError) => {
      logC.warning("caught a StateError of type {} with messsage: {}", se.getClass.toString, se.getMessage)
      handleStateError(se)
    }
    case (t: Throwable) => {
      logC.warning("caught a throwable {}, with message: {}", t.getClass.toString, t.getMessage)
      resJson {
        complete {
          StatusCodes.InternalServerError -> throwableToJson(t)
        }
      }
    }
  }
}
