package chatless.ops.sequencers

import chatless._
import chatless.model._
import chatless.db._
import akka.actor.{ActorSelection, ActorRef}

import scalaz.syntax.id._

trait Sequencer {
  val eventActor: ActorSelection
  type Sequenced = WriterTEither[List[Event], String, Boolean]

  val senderRef: ActorRef

  private def sendWritten(op: Sequenced)(implicit sender: ActorRef) {
    for (events <- op.written) { //events is List[Event]
      eventActor ! events
    }
  }

  private val getValue: Sequenced => WriteStat = _.value

  /** Evaluate sequence, send the list of events to the event actor, return the boolean result from the sequence. */
  def runSequence(sequence: => Sequenced)(implicit sender: ActorRef = senderRef): WriteStat = 
    sequence <| sendWritten |> getValue // kestrel-thrush 1-2 punch.

}

