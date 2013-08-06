package chatless.db

import chatless._

import akka.actor.ActorSelection
import akka.pattern.AskableActorSelection
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import chatless.op2._
import chatless.op2.Operation
import chatless.op2.ResUser
import argonaut.CodecJson
import chatless.models.{MessageM, UserM, TopicM}

class DatabaseActorClient(actorSel: ActorSelection)(implicit context: ExecutionContext, timeout: Timeout) extends DatabaseAccessor {
  val askable = new AskableActorSelection(actorSel)

  def getUser(cid: UserId, uid: UserId): Future[UserM] = (askable ? Operation(cid, ResUser(uid), GetRes)) map {
    case um: UserM => um
    case se: StateError => throw se
    case _ => throw new Exception("wtf")
  }

  def getTopic(cid: UserId, tid: TopicId): Future[TopicM] = (askable ? Operation(cid, ResTopic(tid), GetRes)) map {
    case tm: TopicM => tm
    case se: StateError => throw se
    case _ => throw new Exception("wtf")
  }

  def updateUser(cid: UserId, uid: UserId, spec: UpdateSpec with ForUsers): Future[Boolean] = (askable ?
    Operation(cid, ResUser(uid), spec)) map {
    case b: Boolean => b
    case se: StateError => throw se
    case _ => throw new Exception("wtf")
  }

  def updateTopic(cid: UserId, tid: TopicId, spec: UpdateSpec with ForTopics): Future[Boolean] = (askable ?
    Operation(cid, ResTopic(tid), spec)) map {
    case b: Boolean => b
    case se: StateError => throw se
    case _ => throw new Exception("wtf")
  }

  def getMessages(cid: UserId, tid: TopicId, spec: GetRelative): Future[List[MessageM]] = (askable ? Operation(cid, ResMessages(tid), spec)) map {
    case l:List[_] => l flatMap { i => if (i.isInstanceOf[MessageM]) Some(i.asInstanceOf[MessageM]) else None }
    case se: StateError => throw se
    case _ => throw new Exception("wtf")
  }
}
