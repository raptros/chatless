package chatless.db

import chatless._
import chatless.model.{TopicCoordinate, UserCoordinate, JDoc, Topic}
import com.mongodb.casbah.Imports._

trait TopicDAO {

  def getTopic(coordinate: TopicCoordinate): Option[Topic]

  def listUserTopics(coordinate: UserCoordinate): List[TopicCoordinate]


}
