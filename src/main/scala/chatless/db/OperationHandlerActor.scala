package chatless.db

import chatless._

import argonaut._
import Argonaut._

import com.mongodb.casbah.Imports._

import akka.actor.Actor
import akka.actor.Status
import chatless.op2._
import chatless.db.handlers._
import com.google.inject.Inject


class OperationHandlerActor @Inject() (
    val handleUserOperation:  UserOpHandler,
    val handleTopicOperation: TopicOpHandler)
  extends Actor {


  def receive = {
    case op: Operation => try {
      val res = handleOperation(op)
      sender ! res
    } catch {
      case t: Throwable => sender ! Status.Failure(t)
    }
    case x => sender ! Status.Failure(UnhandleableMessage(x))
  }

  def handleOperation(op: Operation): Any = op match {
    case UserOp(cid, uid, spec) => handleUserOperation(cid, uid, spec)
    case TopicOp(cid, tid, spec) => handleTopicOperation(cid, tid, spec)
    case MessagesOp(cid, tid, spec) => handleMessagesOperations(cid, tid, spec)
    case EventOp(cid, spec) => handleEventOperation(cid, spec)
  }

  def handleMessagesOperations(uid: UserId, tid: TopicId, spec: Specifier with ForMessages): Any = spec match {
    case GetFirst(c) =>
    case GetLast(c) =>
    case GetAt(baseId, c) =>
    case GetBefore(baseId, c) =>
    case GetFrom(baseId, c) =>
    case GetAfter(baseId, c) =>
    case CreateMessage(json) =>
  }

  def handleEventOperation(cid: UserId, spec: Specifier with ForEvents): Any = spec match {
    case GetLast(c) =>
    case GetAt(baseId, c) =>
    case GetBefore(baseId, c) =>
    case GetFrom(baseId, c) =>
    case GetAfter(baseId, c) =>
  }

}

