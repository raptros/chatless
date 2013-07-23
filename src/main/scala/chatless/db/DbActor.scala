package chatless.db

import chatless._
import chatless.operation._
import chatless.operation.ResTopic
import chatless.operation.ResUser

import spray.http._
import MediaTypes._

import argonaut._
import Argonaut._

import scala.concurrent._

import com.mongodb.casbah.Imports._

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Status
import akka.event.Logging
import com.mongodb.casbah


class DbActor(val mc:MongoClient) extends Actor with MeOps {
  val db = mc("chatless")

  val topics = db("topics")
  val users:MongoCollection = db("users")
  val events = db("events")
  val messages = db("messages")

  def this() = this (MongoClient())

  def receive = {
    case op:Operation => try {
      val res = handleOperation(op)
      sender ! res
    } catch {
      case t:Throwable => sender ! Status.Failure(t)
    }
    case x => sender ! Status.Failure(UnhandleableMessage(x))
  }

  def handleOperation(op:Operation):Json = op match {
    case Operation(cid, ResMe, spec) => handleMe(cid, spec)
    case Operation(cid, ResUser(ruid), spec) => handleUserOperation(cid, ruid, spec)
    case Operation(cid, ResTopic(rtid), spec) => jEmptyObject
  }

  def handleUserOperation(cid:UserId, ruid:UserId, spec:OpSpec):Json = spec match {
    case GetAll => getUser(cid, ruid)
    case GetField(f) => {
      val user = getUser(cid, ruid)
      user field f getOrElse jEmptyObject
    }
    case _ => throw OperationNotSupported(cid, ResUser(ruid), spec)
  }

  def getUser(cid:UserId, ruid:UserId):Json = if (cid == ruid) {
    ("uid" := ruid) ->: ("nick" := "...") ->: ("public" := true) ->: ("info" := jEmptyObject) ->: jEmptyObject
  } else {
    ("uid" := ruid) ->: ("nick" := "...") ->: jEmptyObject
  }

  def handleTopicOperation(cid:UserId, rtid:TopicId, spec:OpSpec):Json = spec match {
    case GetAll => getTopic(cid, rtid)
    case GetField(f) => {
      val topic = getTopic(cid, rtid)
      topic field f getOrElse jEmptyObject
    }
    case _ => throw OperationNotSupported(cid, ResTopic(rtid), spec)
  }

  def getTopic(cid:UserId, rtid:TopicId):Json = ("tid" := rtid) ->:
    ("title" := "duck lords") ->:
    ("public" := true) ->:
    ("info" := jEmptyObject ) ->:
    jEmptyObject

}

