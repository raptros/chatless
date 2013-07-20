package chatless.services

import spray.routing._
import spray.http._
import MediaTypes._

import argonaut._

import spray.util.LoggingContext
import chatless.db._

/** defines the chatless service */
trait Service extends MeApi with TopicApi with UserApi with EventApi with TaggedApi {
  def completeStateError(err:StateError, code:StatusCode) = respondWithMediaType(`application/json`) {
    complete { code -> err.asJson.spaces2 }
  }

  def handleStateError(se:StateError) = se match {
    case e:TopicNotFoundError    => completeStateError(e, StatusCodes.NotFound)
    case e:OperationNotSupported => completeStateError(e, StatusCodes.MethodNotAllowed)
    case e:UnhandleableMessage   => completeStateError(e, StatusCodes.InternalServerError)
  }

  implicit def serviceHandler(implicit log:LoggingContext) = ExceptionHandler {
    case (se:StateError) => log.warning(se.getMessage); handleStateError(se)
  }

  val allApis:DOperation = meApi | userApi | topicApi | eventApi | taggedApi

  val finishApis = allApis { dbReq =>
    onSuccess(dbActor ? dbReq) {
      case (json:Json) => respondWithMediaType(`application/json`) { complete { json.nospaces } }
      case _ => throw new Exception("wtf")
    }
  }

  /** Route entry point. */
  val chatlessApi = finishApis ~ path(PathEnd) {
    get {
      complete("yo")
    }
  }

}
