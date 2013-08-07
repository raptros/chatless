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
}
