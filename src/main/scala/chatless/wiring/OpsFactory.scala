package chatless.wiring

import akka.actor.ActorRef
import chatless.ops.{TopicOps, UserOps}

trait OpsFactory {
  def createUserOps(actorRef: ActorRef): UserOps

  def createTopicOps(actorRef: ActorRef): TopicOps

}
