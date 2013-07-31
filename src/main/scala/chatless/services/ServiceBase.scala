package chatless.services

import akka.actor.{ActorRef, ActorSelection}
import akka.util.Timeout

import spray.routing.{ExceptionHandler, RequestContext, HttpService, Directive1}

import scala.reflect.runtime.universe._

import spray.httpx.unmarshalling.Unmarshaller
import argonaut._
import Argonaut._

import scala.concurrent.duration._
import spray.httpx.encoding.NoEncoding
import akka.pattern.AskableActorSelection
import scala.concurrent.{ExecutionContext, Future}
import spray.routing.authentication._
import chatless._
import chatless.operation.{OpSpec, OpRes, Operation}
import chatless.db._
import spray.http._
import MediaTypes._
import spray.util.LoggingContext
import chatless.op2.{UserM}

trait ServiceBase extends HttpService {
  type DOperation = Directive1[Operation]
  val operation:(UserId, OpRes, OpSpec) => Operation = Operation.apply _

  def putReplacement[A](um:Unmarshaller[A]):Directive1[A] = put & decodeRequest(NoEncoding) & entity(um)

  implicit def executor:ExecutionContext

  implicit val timeout = Timeout(5.seconds) // needed for `?` below

  val dbac:DatabaseAccessor

  def dEntity[A](um:Unmarshaller[A]):Directive1[A] = decodeRequest(NoEncoding) & entity(um)

  def userAuth:Directive1[UserId] = authenticate { getUserAuth }

  def getUserAuth:ContextAuthenticator[UserId]

  def dbSel:ActorSelection

  def dbActor = new AskableActorSelection(dbSel)

  def completeStateError(err:StateError, code:StatusCode) = respondWithMediaType(`application/json`) {
    complete { code -> err.asJson.spaces2 }
  }

  def handleStateError(se:StateError) = se match {
    case e:TopicNotFoundError    => completeStateError(e, StatusCodes.NotFound)
    case e:OperationNotSupported => completeStateError(e, StatusCodes.MethodNotAllowed)
    case e:UnhandleableMessage   => completeStateError(e, StatusCodes.InternalServerError)
    case e:AccessNotPermitted => completeStateError(e, StatusCodes.Forbidden)
    case e:NonExistentField => completeStateError(e, StatusCodes.NotFound)
  }

  implicit def serviceHandler(implicit log:LoggingContext) = ExceptionHandler {
    case (se:StateError) => log.warning(se.getMessage); handleStateError(se)
  }

  def callDbActor(dbReq:Operation) = onSuccess(dbActor ? dbReq) {
    case (json:Json) => respondWithMediaType(`application/json`) { complete { json.nospaces } }
    case _ => throw new Exception("wtf")
  }

  def completeAsJson[A:EncodeJson](a:A) = respondWithMediaType(`application/json`) { complete { a.asJson.nospaces } }


  def getType[A:TypeTag](a:A) = typeOf[A]


}
