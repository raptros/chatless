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

class DatabaseActorClient(actorSel:ActorSelection)(implicit context:ExecutionContext, timeout:Timeout) extends DatabaseAccessor {
  val askable = new AskableActorSelection(actorSel)

  def getUser(cid:UserId, uid:UserId):Future[UserM] = (askable ? Operation(cid, ResUser(uid), GetRes)) map {
    case um:UserM => um
    case se:StateError => throw se
    case _ => throw new Exception("wtf")
  }

  def updateUser(cid:UserId, uid:UserId, spec: UpdateSpec with ForUsers):Future[Boolean] = (askable ?
    Operation(cid, ResUser(uid), spec)) map {
    case b:Boolean => b
    case se:StateError => throw se
    case _ => throw new Exception("wtf")
  }
}
