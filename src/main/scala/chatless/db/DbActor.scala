package chatless.db

import spray.routing._

import spray.http._
import MediaTypes._

import argonaut._
import Argonaut._

import shapeless._

import scala.concurrent._
import ExecutionContext.Implicits.global

import com.mongodb.casbah.Imports._

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Status
import akka.event.Logging
import chatless._


class DbActor(val mc:MongoClient) extends Actor {
  val db = mc("chatless")

  val topics = db("topics")
  val users = db("users")
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
    case Operation(cid, ResUser(ruid), spec) => handleUserOperation(cid, ruid, spec)
    case Operation(cid, ResTopic(rtid), spec) => jEmptyObject
  }

  def handleUserOperation(cid:UserId, ruid:UserId, spec:OpSpec):Json = spec match {
    case GetAll => getUser(cid, ruid)
    case GetFields(fields @ _*) => {
      val user = getUser(cid, ruid)
      (fields flatMap { user field _ } fold jEmptyObject) { _ deepmerge _ }
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
    case GetFields(fields @ _*) => {
      val topic = getTopic(cid, rtid)
      (fields flatMap { topic field _ } fold jEmptyObject) { _ deepmerge _ }
    }
    case _ => throw OperationNotSupported(cid, ResTopic(rtid), spec)
  }

  def getTopic(cid:UserId, rtid:TopicId):Json = ("tid" := rtid) ->:
    ("title" := "duck lords") ->:
    ("public" := true) ->:
    ("info" := jEmptyObject ) ->:
    jEmptyObject

}

