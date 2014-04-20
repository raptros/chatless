package chatless

import scalaz._

package object db {

  type WriteStat = String \/ Boolean

  val EVENT_SEQUENCE_ID = "eventSequence"

  def TOPIC_MESSAGE_SEQUENCE_ID(tid: TopicId) = s"topicSequence_$tid"
}
