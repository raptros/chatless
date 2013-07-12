package chatless

import akka.actor.{Deploy, ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import chatless.db.DbActor
import chatless.services.ServiceActor

object Boot extends App {
 // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("chatless-system")

  // create and start our service actor
  val service = system.actorOf(Props[ServiceActor], "chatless-service")

  val db = system.actorOf(Props(new DbActor), "chatless-service-db")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}
