package chatless.ops.topic

import chatless.model.{Message, User}
import chatless.model.topic.Topic
import chatless.ops.OperationResult

trait SendMessageImpl { this: TopicOps with ImplUtils =>

  override def sendMessage(caller: User, topic: Topic, message: Message): OperationResult[Message] = ???

}
