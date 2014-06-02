package chatless.db.mongo

import scalaz._
import chatless.model.ids._

trait IdGenerator {

  def nextTopicId(): String @@ TopicId

  def nextMessageId(): String @@ MessageId
}
