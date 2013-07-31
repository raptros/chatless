package chatless.db

import chatless._

import akka.actor.ActorSelection
import akka.pattern.AskableActorSelection
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import chatless.op2.{GetUser, UserM}

class DatabaseActorClient(actorSel:ActorSelection)(implicit context:ExecutionContext, timeout:Timeout) extends DatabaseAccessor {
  val askable = new AskableActorSelection(actorSel)

  def getUser(cid:UserId, uid:UserId):Future[UserM] = (askable ? GetUser(uid)) map {
    case um:UserM => um
    case se:StateError => throw se
    case _ => throw new Exception("wtf")
  }

}
