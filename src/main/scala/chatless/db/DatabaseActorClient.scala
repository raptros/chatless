package chatless.db

import chatless._

import akka.actor.ActorSelection
import akka.pattern.AskableActorSelection
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import chatless.op2._
import chatless.op2.Operation
import argonaut.{Json, CodecJson}
import chatless.models._
import chatless.op2.TopicOp
import chatless.op2.UserOp
import chatless.op2.MessagesOp
import scala.Some
import chatless.op2.CreateMessage
import com.google.inject.Inject
import shapeless._
import shapeless.Typeable._
import chatless.wiring.params.DbActorSelection
import com.google.inject.assistedinject.Assisted

class DatabaseActorClient @Inject() (
    @DbActorSelection actorSel: ActorSelection,
    to: Timeout)
  extends DatabaseAccessor {

  implicit val timeout = to

  val askable = new AskableActorSelection(actorSel)

  def getUser(cid: UserId, uid: UserId)(implicit context: ExecutionContext) =
    (askable ? UserOp(cid, uid, GetRes)) map {
      case um: UserM => um
      case se: StateError => throw se
      case _ => throw new Exception("wtf")
    }

  def getTopic(cid: UserId, tid: TopicId)(implicit context: ExecutionContext) =
    (askable ? TopicOp(cid, tid, GetRes)) map {
      case tm: TopicM => tm
      case se: StateError => throw se
      case _ => throw new Exception("wtf")
    }

  def updateUser(cid: UserId, uid: UserId, spec: UpdateSpec with ForUsers)(implicit context: ExecutionContext) =
    (askable ? UserOp(cid, uid, spec)) map {
      case b: Boolean => b
      case se: StateError => throw se
      case _ => throw new Exception("wtf")
    }

  def updateTopic(cid: UserId, tid: TopicId, spec: UpdateSpec with ForTopics)(implicit context: ExecutionContext) =
    (askable ? TopicOp(cid, tid, spec)) map {
      case b: Boolean => b
      case se: StateError => throw se
      case _ => throw new Exception("wtf")
    }

  def getMessages(cid: UserId, tid: TopicId, spec: GetRelative with ForMessages)(implicit context: ExecutionContext) =
    (askable ? MessagesOp(cid, tid, spec)) map {
      case l:List[_] => l flatMap { i => i.cast[MessageM] }
      case se: StateError => throw se
      case _ => throw new Exception("wtf")
    }

  def createMessage(cid: UserId, tid: TopicId, j: Json)(implicit context: ExecutionContext) =
    (askable ? MessagesOp(cid, tid, CreateMessage(j))) map {
      case ce: CreateHandle => ce
      case se: StateError => throw se
      case _ => throw new Exception("wtf")
    }

  def getEvents(cid: UserId, spec: GetRelative with ForEvents)(implicit context: ExecutionContext) =
    (askable ? EventOp(cid, spec)) map {
      case l: List[_] => l flatMap { i => i.cast[Event] }
      case se: StateError => throw se
      case _ => throw new Exception("wtf")
    }
}
