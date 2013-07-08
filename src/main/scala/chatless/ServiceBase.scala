package chatless

import akka.actor.ActorSelection
import chatless.db.Operation
import akka.util.Timeout

import spray.routing.{RequestContext, HttpService, Directive1}

import spray.httpx.unmarshalling.Unmarshaller

import scala.concurrent.duration._
import spray.httpx.encoding.NoEncoding
import akka.pattern.AskableActorSelection
import scala.concurrent.Future
import spray.routing.authentication._

trait ServiceBase extends HttpService {
  type DOperation = Directive1[Operation]

  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5.seconds) // needed for `?` below

  def dEntity[A](um:Unmarshaller[A]):Directive1[A] = decodeRequest(NoEncoding) & entity(um)

  def userAuth:Directive1[UserId] = authenticate { getUserAuth }

  def getUserAuth:ContextAuthenticator[UserId]

  def dbSel:ActorSelection

  def dbActor = new AskableActorSelection(dbSel)
}
