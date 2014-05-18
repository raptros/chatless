package chatless.db.mongo

import chatless.model.{TopicCoordinate, UserCoordinate}

trait IdGenerator {

  def nextTopicId(): String

  def nextMessageId(): String
}
