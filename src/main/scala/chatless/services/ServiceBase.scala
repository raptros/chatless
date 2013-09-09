package chatless.services


import spray.routing._
import spray.util.LoggingContext
import spray.http._
import MediaTypes._
import spray.httpx.encoding.NoEncoding
import spray.httpx.unmarshalling._

import shapeless._
import shapeless.Typeable._

import scalaz._
import scalaz.syntax.apply._
import scalaz.std.list._
import scalaz.std.option._
import scalaz.syntax.std.boolean._

import org.json4s._
import org.json4s.native.JsonMethods._

import spray.httpx.Json4sSupport

import chatless.responses._
import chatless.model.{JDoc, JDocSerializer}
import chatless.db.WriteStat
import spray.http.HttpHeaders.RawHeader
import akka.event.LoggingAdapter
import spray.routing.RequestContext
import spray.http.HttpHeaders.RawHeader
import chatless.responses.OperationNotSupportedError
import scala.Some
import spray.http.HttpResponse
import chatless.responses.ModelExtractionError
import chatless.responses.TopicNotFoundError
import chatless.responses.UnhandleableMessageError
import chatless.responses.UserNotFoundError

trait ServiceBase extends HttpService with Json4sSupport {

  val log: LoggingAdapter

  implicit val json4sFormats = chatless.json4sFormats

  def optionJsonEntity: Directive1[Option[JDoc]] = extract { c: RequestContext =>
    for {
      ent <- c.request.entity.toOption
      str = ent.asString
      jsv <- parseOpt(str)
      obj <- jsv.cast[JObject]
    } yield JDoc(obj.obj)
  }

  def dEntity[A](um: Unmarshaller[A]): Directive1[A] = decodeRequest(NoEncoding) & entity(um)


  def resJson: Directive0 = respondWithMediaType(`application/json`)

  def resText: Directive0 = respondWithMediaType(`text/plain`)

  def setCompletion(pathMap: Map[String, Set[String]]): Route = {
    path(pathMap / Segment / PathEnd) { (set: Set[String], v: String) =>
      complete { if (set contains v) StatusCodes.NoContent else StatusCodes.NotFound }
    }
  }

  def setCompletion(pathPairs: (String, Set[String])*): Route = setCompletion(pathPairs.toMap)

  def fromString[T](implicit deser: FromStringDeserializer[T]): Unmarshaller[T] = new Deserializer[HttpEntity, T] {
    def apply(v1: HttpEntity): Deserialized[T] = BasicUnmarshallers.StringUnmarshaller(v1).right flatMap { deser }
  }

  def completeDBOp(res: WriteStat)(onUpdated: => Unit) = res match {
    case \/-(true) => onUpdated; respondWithHeader(RawHeader("x-chatless-updated", "yes")) {
      complete(StatusCodes.NoContent)
    }
    case \/-(false) => complete(StatusCodes.NoContent)
    case -\/(msg) =>
      log.warning("failed to complete update because: {}", msg)
      complete(StatusCodes.InternalServerError -> msg)
  }


  def handleStateError(se: StateError) = se match {
    case _: UnhandleableMessageError => se complete StatusCodes.InternalServerError
    case _: UserNotFoundError => se complete StatusCodes.NotFound
    case _: ModelExtractionError => se complete StatusCodes.InternalServerError
    case _: TopicNotFoundError => se complete StatusCodes.NotFound
    case _: OperationNotSupportedError => se complete StatusCodes.BadRequest
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
