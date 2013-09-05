package chatless.services


import spray.routing._
import spray.util.LoggingContext
import spray.http._
import MediaTypes._
import spray.httpx.encoding.NoEncoding
import spray.httpx.unmarshalling._

import shapeless._
import shapeless.Typeable._

import org.json4s._
import org.json4s.native.JsonMethods._

import spray.httpx.Json4sSupport

import chatless.responses._
import chatless.model.{JDoc, JDocSerializer}

trait ServiceBase extends HttpService with Json4sSupport {
  implicit val json4sFormats =
    DefaultFormats ++
    org.json4s.ext.JodaTimeSerializers.all +
    new JDocSerializer

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
      resText { complete { Map("contains" -> (set contains v)) } }
    }
  }

  def setCompletion(pathPairs: (String, Set[String])*): Route = setCompletion(pathPairs.toMap)

  def fromString[T](implicit deser: FromStringDeserializer[T]): Unmarshaller[T] = new Deserializer[HttpEntity, T] {
    def apply(v1: HttpEntity): Deserialized[T] = BasicUnmarshallers.StringUnmarshaller(v1).right flatMap { deser }
  }

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
