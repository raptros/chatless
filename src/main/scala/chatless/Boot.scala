package chatless

import akka.actor.{ActorRef, Deploy, ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import chatless.services.ClientApiActor
import scala.concurrent.duration._

import com.mongodb.casbah.Imports._
import com.google.inject.{Injector, Guice}
import chatless.wiring.{ActorNames, SystemModule}
import chatless.wiring.actors.ActorInjector
import chatless.events.LocalEventReceiver

object Boot extends App {
 // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("chatless-system")

  val injector: Injector = Guice.createInjector(new SystemModule(system))

  val actorProvider = ActorInjector.providerFor(injector)

  import net.codingwell.scalaguice.InjectorExtensions._

  // start up the event receiver
  val eventRecv = system.actorOf(actorProvider.props[LocalEventReceiver], ActorNames.LOCAL_EVENT_RECV)

  // create and start our service actor
  val service = system.actorOf(actorProvider.props[ClientApiActor], ActorNames.CLIENT_API)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 5775)
}
