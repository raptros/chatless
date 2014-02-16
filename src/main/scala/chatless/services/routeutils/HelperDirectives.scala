package chatless.services.routeutils
import chatless.services.{X_CREATED_TOPIC, X_UPDATED}
import spray.routing._
import chatless.model.JDoc
import spray.httpx.Json4sSupport
import org.json4s._
import org.json4s.native.JsonMethods._
import akka.event.LoggingAdapter

import shapeless._
import shapeless.Typeable._
import spray.http._
import spray.http.MediaTypes._
import spray.httpx.unmarshalling._
import chatless.db.WriteStat
import scalaz._
import spray.http.HttpHeaders.RawHeader
import spray.util.LoggingContext

trait HelperDirectives extends Directives with Json4sSupport {
  implicit val json4sFormats = chatless.json4sFormats

  def fPath[L <: HList](pm: PathMatcher[L]): Directive[L] = pathPrefix(pm ~ Slash.? ~ PathEnd)

  def segFin = fPath(Segment)

  def peoss = pathEndOrSingleSlash

  def sEntity[V](um: FromRequestUnmarshaller[V]): Directive1[V] = pathEndOrSingleSlash & entity(um)

  def optionJsonEntity: Directive1[Option[JDoc]] = extract { c: RequestContext =>
    for {
      ent <- c.request.entity.toOption
      str = ent.asString
      jsv <- parseOpt(str)
      obj <- jsv.cast[JObject]
    } yield JDoc(obj.obj)
  }

  val getCount: Directive1[Int] = (pathEndOrSingleSlash & provide(1)) | fPath(IntNumber)

  val getIdAndCount: Directive[String :: Int :: HNil] =
    pathPrefix(Segment) & ConjunctionMagnet.fromDirective(getCount)

  def resJson: Directive0 = respondWithMediaType(`application/json`)

  def resText: Directive0 = respondWithMediaType(`text/plain`)

  def mapFieldsAsJson(fields: Seq[(String, JValue)]): Map[String, JValue] = fields.toMap map {
    case (s, v) => s -> JObject(s -> v)
  }

  def completeFieldsAs(fields: (String, JValue)*) = fPath(mapFieldsAsJson(fields)) { v => resJson { complete(v) } }

  def setCompletion(pathMap: Map[String, Set[String]]): Route = {
    fPath(pathMap / Segment) { (set: Set[String], v: String) =>
      complete { if (set contains v) StatusCodes.NoContent else StatusCodes.NotFound }
    }
  }

  /** a tidyer way to use setCompletion*/
  def completeWithContains(pathPairs: (String, Set[String])*): Route = setCompletion(pathPairs.toMap)

  /** completes with an operation that returns a writestat - i.e. something that updates the database */
  def completeOp(res: => WriteStat)(implicit log: LoggingContext) = res match {
    case \/-(true) => respondWithHeader(RawHeader(X_UPDATED, "yes")) {
      complete(StatusCodes.NoContent)
    }
    case \/-(false) => complete(StatusCodes.NoContent)
    case -\/(msg) =>
      log.warning("failed to complete update because: {}", msg)
      complete(StatusCodes.InternalServerError -> msg)
  }

  /** completes with something that inserts into the database and returns the resulting id (or an error)*/
  def completeCreation(header: String)(res: => String \/ String)(implicit lc: LoggingContext) = res match {
    case \/-(id) => respondWithHeader(RawHeader(header, id)) {
      complete(StatusCodes.NoContent)
    }
    case -\/(msg) =>
      lc.warning("failed to create for {} because: {}", header, msg)
      complete(StatusCodes.InternalServerError -> msg)
  }

  def fromString[T](implicit deser: FromStringDeserializer[T]) = new FromRequestUnmarshaller[T] {
    def apply(v1: HttpRequest): Deserialized[T] = BasicUnmarshallers.StringUnmarshaller(v1.entity).right flatMap { deser }
  }

  /** Maps path components (as prefixes) to CompleterCarriers and executes the route of whichever carrier is selected.
    * @param carriers pairs of path components and completer carriers
    * @return the route of whichever path is taken
    */
  def routeCarriers(carriers: (String, CompleterCarrier)*): Route =
    pathPrefix(valueMap2PathMatcher(carriers.toMap)) { _.route }

}

object HelperDirectives extends HelperDirectives
