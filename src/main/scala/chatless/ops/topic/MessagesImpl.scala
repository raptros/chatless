package chatless.ops.topic

import argonaut.Json
import chatless.model.{RMB, PostedMessage, Message, User}
import chatless.model.topic.Topic
import chatless.ops.OperationTypes._
import chatless.ops.Preconditions._
import chatless.ops.OperationFailure.precondition

trait MessagesImpl { this: TopicOps with ImplUtils =>

  override def postMessage(caller: User, topic: Topic, body: Json) = for {
    mode <- callerEffectiveMode(SEND_MESSAGE, caller, topic)
    _ <- precondition(mode.write, SEND_MESSAGE, WRITE_DENIED, "topic" -> topic.coordinate)
    msg <- sendMessageOp(SEND_MESSAGE, topic.coordinate, RMB.posted(caller.coordinate, body))
  } yield msg
}
