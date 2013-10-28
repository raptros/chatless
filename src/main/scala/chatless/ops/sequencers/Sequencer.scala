package chatless.ops.sequencers

import chatless._
import chatless.model._
import chatless.db._
import chatless.responses.UserNotFoundError
import akka.actor.{Actor, ActorSelection, ActorRef}


trait Sequencer {
  val eventActor: ActorSelection

  val senderRef: ActorRef

  def wrapWS(v: => WriteStat) = wrapEither[Event, String, Boolean](v)

  def stepEB[L](bPrev: Boolean) = stepEither[L, Boolean](bPrev, bPrev) _

  def runSequence(sequence: => WriterTEither[List[Event], String, Boolean])(implicit sender: ActorRef = senderRef): WriteStat = {
    val op = sequence
    op.written foreach { eventActor ! _ }
    op.value
  }

}

