package chatless.db

import chatless._

import argonaut._
import Argonaut._

import com.mongodb.casbah.Imports._

import akka.actor.Actor
import akka.actor.Status
import chatless.op2._


class DbActor(val mc: MongoClient) extends Actor {
  val db = mc("chatless")

  val topics = db("topics")
  val users: MongoCollection = db("users")
  val events = db("events")
  val messages = db("messages")

  def this() = this (MongoClient())

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
    case Operation(cid, ResUser(uid), spec) if (spec.isInstanceOf[ForUsers]) => handleUserOperation(cid, uid, spec)
    case Operation(cid, ResTopic(tid), spec) if (spec.isInstanceOf[ForTopics]) => jEmptyObject
  }

  def handleUserOperation(cid: UserId, ruid: UserId, spec: Specifier): Json = spec match {
    case _ => throw OperationNotSupported(cid, ResUser(ruid), spec)
  }

  def handleTopicOperation(cid: UserId, rtid: TopicId, spec: Specifier): Json = spec match {
    case _ => throw OperationNotSupported(cid, ResTopic(rtid), spec)
  }

}

