package chatless.db

import java.util.Date

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import chatless.{UserId, TopicId, MessageId}

sealed trait Handle

case class UserHandle(uid:UserId, nick:String, public:Boolean) extends Handle

case class TopicHandle(tid:TopicId, opid:UserId, title:String) extends Handle

case class Message(mId:MessageId,
                   tId:TopicId,
                   senderId:UserId,
                   timestamp:Date,
                   replyTo:List[MessageId],
                   data:String) extends Handle with Full {
  type H = Message
  def toHandle = this
}


sealed trait Full {
  type H <: Handle
  def toHandle:H
}

case class User(uid:UserId,
                nick:String,
                public:Boolean,
                info:JObject,
                following:List[UserId],
                followers:List[UserId],
                blocked:List[UserId],
                topics:List[TopicId])