package chatless

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

/** this is the actor for the chatless service. */
class ChatlessServiceActor extends Actor with ChatlessService {
  def actorRefFactory = context

  def receive = runRoute(chatlessApi)
}


/** defines the chatless service */
trait ChatlessService extends HttpService {
  val eventsApi = pathPrefix("events") {
    path()
  }

  val chatlessApi = path("") {
    get {
      respondWithMediaType(`text/plain`) { 
        complete { "yo" }
      }
    }
  }
}
