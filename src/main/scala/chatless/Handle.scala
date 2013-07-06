package chatless

import java.util.Date

sealed trait Handle

case class UserHandle(uid:UserId, nick:String, public:Boolean) extends Handle

case class TopicHandle(tid:TopicId, opid:UserId, title:String)

case class Message(mId:MessageId,
                   tId:TopicId,
                   senderId:UserId,
                   timestamp:Date,
                   replyTo:List[MessageId],
                   data:String) extends Handle
