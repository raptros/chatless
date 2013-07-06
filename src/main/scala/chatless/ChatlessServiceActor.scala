package chatless

import akka.actor.Actor

/** this is the actor for the chatless service. */
class ChatlessServiceActor extends Actor with ChatlessService {
  def actorRefFactory = context

  def receive = runRoute(chatlessApi)
}
