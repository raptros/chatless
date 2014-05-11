package chatless.db

import chatless._
import chatless.model._
import com.mongodb.casbah.Imports._
import scalaz._

trait TopicDAO {

  /** attempts to get a topic, which may fail for various reasons
    * @param coordinate the coordinate specifies the topic to get
    * @return either the topic, or why not the topic
    */
  def get(coordinate: TopicCoordinate): DbError \/ Topic

  /** gets all the available topic coordinates for the user specified
    * @param coordinate the coordinate of a user that should have topics in the instance's database
    * @return the coordinates of those topics
    */
  def listUserTopics(coordinate: UserCoordinate): Iterable[TopicCoordinate]

  /** attempts to insert a topic into the collection.
    * @param topic fully prepared topic
    * @return either the inserted topic id or an error
    */
  def insertUnique(topic: Topic): DbError \/ String

  /** creates a topic and attempts to insert it. will perform retries if the init does not specify an ID.
    * @param user the ID string of a user local to this instance.
    * @param init the
    * @return
    */
  def createLocal(user: UserId, init: TopicInit): DbError \/ String

}
