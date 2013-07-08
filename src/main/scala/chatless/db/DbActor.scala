package chatless.db

import spray.routing._

import spray.http._
import MediaTypes._

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import shapeless._

import scala.reflect.runtime.universe._

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

  def handleOperation(op:Operation):JValue = op match {
    case Operation(cid, ResUser(ruid), spec) => handleUserOperation(cid, ruid, spec)
    case Operation(cid, ResTopic(rtid), spec) => JObject()
  }

  def handleUserOperation(cid:UserId, ruid:UserId, spec:OpSpec):JValue = spec match {
    case GetAll => getUser(cid, ruid)
    case GetFields(fields @ _*) => JObject(getUser(cid, ruid) filterField  { fields contains _._1 })
    case _ => throw OperationNotSupported(cid, ResUser(ruid), spec)
  }

  def getUser(cid:UserId, ruid:UserId):JValue = if (cid == ruid) {
    pair2Assoc("uid" -> ruid) ~ ("nick" -> "...") ~
      ("public" -> true) ~ ("info" -> JObject())
  } else {
    pair2Assoc("uid" -> ruid) ~ ("nick" -> "...")
  }

  def handleTopicOperation(cid:UserId, rtid:TopicId, spec:OpSpec):JValue = spec match {
    case GetAll => getTopic(cid, rtid)
    case GetFields(fields @ _*) => JObject(getTopic(cid, rtid) filterField  { fields contains _._1 })
    case _ => throw OperationNotSupported(cid, ResTopic(rtid), spec)
  }

  def getTopic(cid:UserId, rtid:TopicId) = {
    pair2Assoc("tid" -> rtid) ~ ("title" -> "duck lords") ~
      ("public" -> true) ~ ("info" -> JObject() )
  }

}

