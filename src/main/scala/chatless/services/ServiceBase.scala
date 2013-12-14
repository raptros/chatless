package chatless.services


import spray.routing._
import spray.util.LoggingContext
import spray.http._
import MediaTypes._

import scalaz._
import scalaz.syntax.apply._
import scalaz.std.list._
import scalaz.std.option._
import scalaz.syntax.std.boolean._

import org.json4s._
import org.json4s.native.JsonMethods._

import spray.httpx.Json4sSupport

import chatless.responses._
import akka.event.LoggingAdapter
import chatless.responses.ModelExtractionError
import chatless.responses.TopicNotFoundError
import chatless.responses.UnhandleableMessageError
import chatless.responses.UserNotFoundError
import akka.actor.ActorRef
import chatless.services.routeutils.HelperDirectives

trait ServiceBase extends HttpService with Json4sSupport with HelperDirectives {

  val log: LoggingAdapter

  implicit val implLog = log


  def handleStateError(se: StateError) = se match {
    case _: UnhandleableMessageError => se complete StatusCodes.InternalServerError
    case _: UserNotFoundError => se complete StatusCodes.NotFound
    case _: ModelExtractionError => se complete StatusCodes.InternalServerError
    case _: TopicNotFoundError => se complete StatusCodes.NotFound
  }

  def throwableToJson(t: Throwable): JObject = {
    val tpe: Option[JField] = Some("type" -> JString(t.getClass.toString))
    val msg: Option[JField] = Option(t.getMessage) map { s => JString(s) } map { "message".-> }
    val trace: List[JString] = for {
      gst <- Option(t.getStackTrace).toList
      elem <- gst.toList
    } yield JString(elem.toString)
    val tracePair: Option[JField] = if (trace.nonEmpty) Some("trace" -> JArray(trace.toList)) else None
    val cause: Option[JField] = Option(t.getCause) map { c => "cause" -> throwableToJson(c) }
    val fields: List[JField] = List(tpe, msg, tracePair, cause).flatten
    JObject(fields)
  }

  implicit def serviceHandler(implicit loggingContext: LoggingContext) = ExceptionHandler {
    case (se: StateError) => {
      log.warning("caught a StateError of type {} with messsage: {}", se.getClass.toString, se.getMessage)
      handleStateError(se)
    }
    case (t: Throwable) => {
      log.warning("caught a throwable {}, with message: {}", t.getClass.toString, t.getMessage)
      resJson {
        complete {
          StatusCodes.InternalServerError -> throwableToJson(t)
        }
      }
    }
  }
}
