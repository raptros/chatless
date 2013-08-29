package chatless.services


import spray.routing._
import spray.util.LoggingContext
import spray.http._
import MediaTypes._
import spray.httpx.encoding.NoEncoding
import spray.httpx.unmarshalling._


import scala.concurrent.Future

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

import chatless.db._
import spray.httpx.Json4sSupport

trait ServiceBase extends HttpService with Json4sSupport {
  implicit val json4sFormats = org.json4s.DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all


    def optionJsonEntity: Directive1[Option[Map[String, Any]]] = extract { c: RequestContext =>
      for {
        ent <- c.request.entity.toOption map { _.asString }
        js <- parseOpt(ent)
        m <- js.extractOpt[Map[String, Any]]
      } yield m
  }

  def dEntity[A](um: Unmarshaller[A]): Directive1[A] = decodeRequest(NoEncoding) & entity(um)


  def resJson: Directive0 = respondWithMediaType(`application/json`)

  def resText: Directive0 = respondWithMediaType(`text/plain`)

  /*
  def filterJson(json: Json, fields: List[String]): Json = {
    val mapped = fields map { f => f :=? (json -| f) }
    (mapped foldLeft  jEmptyObject) { _.->?:(_) }
  }

  implicit def executor: ExecutionContext = actorRefFactory.dispatcher

  def completeJson[A: EncodeJson](a: A): Route = respondWithMediaType(`application/json`) {
    complete(a.asJson.nospaces)
  }

  def completeJson[A: EncodeJson](a: => Future[A]): Route = respondWithMediaType(`application/json`) {
    complete(a map { _.asJson.nospaces })
  }

*/
  def mkRes(v: Any): JValue = "r" -> v

  def handleStateError(se: StateError) = se match {
    case _: UnhandleableMessageError => se complete StatusCodes.InternalServerError
    case _: UserNotFoundError => se complete StatusCodes.NotFound
    case _: ModelExtractionError => se complete StatusCodes.InternalServerError
    case _: TopicNotFoundError => se complete StatusCodes.NotFound
    case _: OperationNotSupportedError => se complete StatusCodes.BadRequest
  }

  implicit def serviceHandler(implicit log: LoggingContext) = ExceptionHandler {
    case (se: StateError) => log.warning(se.getMessage); handleStateError(se)
    case (t: Throwable) => log.warning(t.getMessage); respondWithMediaType(`text/plain`) {
      complete { StatusCodes.InternalServerError -> "failed"}
    }
  }
}
