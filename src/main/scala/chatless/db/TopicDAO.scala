package chatless.db

import chatless._
import chatless.model._
import com.mongodb.casbah.Imports._
import scalaz._

trait TopicDAO {

  def getTopic(coordinate: TopicCoordinate): DbError \/ Topic

  def listUserTopics(coordinate: UserCoordinate): Seq[TopicCoordinate]

  def createLocalTopic(user: UserId, init: TopicInit): DbError \/ String

}
