package chatless.services

import akka.actor.Actor
import spray.routing.authentication._

import com.mongodb.casbah.Imports._
import spray.routing.RequestContext
import scala.concurrent._
import chatless._

/** this is the actor for the chatless service. */
class ServiceActor extends Actor with Service {
  def getUserAuth:ContextAuthenticator[UserId] = BasicAuth("", _.user)

  def dbSel = context.actorSelection("../chatless-service-db")

  def actorRefFactory = context

  def receive = runRoute(chatlessApi)
}
