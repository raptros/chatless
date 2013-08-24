package chatless.services
import chatless._

import akka.util.Timeout

import spray.routing._
import spray.util.LoggingContext
import spray.http._
import MediaTypes._
import spray.httpx.encoding.NoEncoding
import spray.httpx.marshalling.Marshaller._
import spray.httpx.unmarshalling._


import argonaut._
import Argonaut._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

import chatless.db._

trait ServiceBase extends HttpService {

  def optionJsonEntity: Directive1[Option[Json]] = extract { c =>
    c.request.entity.toOption map { _.asString } flatMap { _.parseOption }
  }

  def filterJson(json: Json, fields:List[String]): Json = {
    val mapped = fields map { f => f :=? (json -| f) }
    (mapped :\ jEmptyObject) { _ ->?: _ }
  }

  implicit def executor: ExecutionContext = actorRefFactory.dispatcher

  def dbac: DatabaseAccessor

  def dEntity[A](um: Unmarshaller[A]): Directive1[A] = decodeRequest(NoEncoding) & entity(um)


  def completeString(s: String): Route = respondWithMediaType(`text/plain`) {
    complete { s }
  }

  def completeString(s: => Future[String]): Route = respondWithMediaType(`text/plain`) {
    complete { s }
  }

  def completeBoolean(b: Boolean): Route = respondWithMediaType(`text/plain`) {
    complete { b }
  }

  def completeBoolean(b: => Future[Boolean]): Route = respondWithMediaType(`text/plain`) {
    complete { b }
  }

  def completeJson[A: EncodeJson](a: A): Route = respondWithMediaType(`application/json`) {
    complete(a.asJson.nospaces)
  }

  def completeJson[A: EncodeJson](a: => Future[A]): Route = respondWithMediaType(`application/json`) {
    complete(a map { _.asJson.nospaces })
  }

  def completeStateError(err: StateError, code: StatusCode) = respondWithMediaType(`application/json`) {
    complete { code -> err.asJson.spaces2 }
  }

  def handleStateError(se: StateError) = se match {
    case e: TopicNotFoundError    => completeStateError(e, StatusCodes.NotFound)
    case e: OperationNotSupported => completeStateError(e, StatusCodes.MethodNotAllowed)
    case e: UnhandleableMessage   => completeStateError(e, StatusCodes.InternalServerError)
    //    case e: AccessNotPermitted => completeStateError(e, StatusCodes.Forbidden)
    //    case e: NonExistentField => completeStateError(e, StatusCodes.NotFound)
  }

  implicit def serviceHandler(implicit log: LoggingContext) = ExceptionHandler {
    case (se: StateError) => log.warning(se.getMessage); handleStateError(se)
    case (t: Throwable) => log.warning(t.getMessage); respondWithMediaType(`application/json`) {
      complete { StatusCodes.InternalServerError -> ("failed".asJson -->>: jEmptyArray).nospaces }
    }
  }

}
