package chatless.services

import akka.actor.Actor
import spray.routing.authentication._

import com.mongodb.casbah.Imports._
import spray.routing.RequestContext
import scala.concurrent._
import chatless._
import chatless.db.DatabaseActorClient
import akka.util.Timeout
import scala.concurrent.duration._

/** this is the actor for the chatless service. */
class ServiceActor extends Actor with Service {
  def getUserAuth:ContextAuthenticator[UserId] = BasicAuth("", _.user)

  implicit val timeout = Timeout(5.seconds) // needed for `?` below

  def dbSel = context.actorSelection("../chatless-service-db")

  val dbac = new DatabaseActorClient(dbSel)

  def actorRefFactory = context

  def receive = runRoute(chatlessApi)

}
