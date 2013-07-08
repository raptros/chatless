package chatless

import spray.routing._
import spray.http._
import spray.httpx.unmarshalling.Unmarshaller
import MediaTypes._

import org.json4s._
import org.json4s.native.JsonMethods._

import shapeless._

import scala.concurrent.duration._
import spray.util.LoggingContext
import akka.actor.ActorSelection
import akka.pattern.AskableActorSelection
import akka.util.Timeout
import chatless.db._
import shapeless.::
import chatless.db.Operation
import spray.httpx.encoding.NoEncoding

/** defines the chatless service */
trait Service extends Topics with Users {
  val eventsBase:Directive0 = pathPrefix("events")

  def completeStateError(err:StateError, code:StatusCode) = respondWithMediaType(`application/json`) {
    complete { code -> compact(render(ToJson(err))) }
  }

  def handleStateError(se:StateError) = se match {
    case e:TopicNotFoundError    => completeStateError(e, StatusCodes.NotFound)
    case e:OperationNotSupported => completeStateError(e, StatusCodes.MethodNotAllowed)
    case e:UnhandleableMessage   => completeStateError(e, StatusCodes.InternalServerError)
  }

  implicit def serviceHandler(implicit log:LoggingContext) = ExceptionHandler {
    case (se:StateError) => log.warning(se.getMessage); handleStateError(se)
  }

  val allApis:DOperation = topicsApi | userApi

  val finishApis = allApis { dbReq =>
    onSuccess(dbActor ? dbReq) {
      case (jv:JValue) => respondWithMediaType(`application/json`) { complete { compact(render(jv))  } }
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
