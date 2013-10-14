package chatless.sequencers

import chatless._
import chatless.model._
import chatless.db._
import chatless.responses.UserNotFoundError
import akka.actor.ActorRef


trait Sequencer {
  val userDao: UserDAO
  val topicDao: TopicDAO

  val eventActor: ActorRef

  val getUser = (uid: UserId) => userDao get uid getOrElse { throw UserNotFoundError(uid) }

  def wrapWS(v: => WriteStat) = wrapEither[Event, String, Boolean](v)

  def runSequence(sequence: => WriterTEither[List[Event], String, Boolean]): WriteStat = {
    val op = sequence
    op.written foreach { eventActor ! _ }
    op.value
  }

}
