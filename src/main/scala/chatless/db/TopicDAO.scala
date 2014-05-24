package chatless.db

import chatless._
import chatless.model._
import com.mongodb.casbah.Imports._
import scalaz._
import chatless.model.topic.{TopicInit, Topic}
import argonaut._

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
    * @return either the inserted topic or an error
    */
  def insertUnique(topic: Topic): DbError \/ Topic

  /** creates a topic and attempts to insert it. will perform retries if the init does not specify an ID.
    * @param user the ID string of a user local to this instance.
    * @param init an init object
    * @return the new topic, or an error
    */
  def createLocal(user: String, init: TopicInit): DbError \/ Topic

  /** updates a currently existing topic
    * @param topic a topic that currently exists within the database (i.e. the topic coordinate can be gotten)
    * @return an error or the saved version of the topic.
    */
  def save(topic: Topic): DbError \/ Topic

  protected def modify(tc: TopicCoordinate, adjust: Topic => Topic): DbError \/ Topic = for {
    topic <- get(tc)
    newTopic = adjust(topic)
    saved <- save(newTopic)
  } yield saved

  def setBanner(tc: TopicCoordinate, banner: String): DbError \/ Topic = modify(tc, _.copy(banner = banner))

  def setInfo(tc: TopicCoordinate, info: Json): DbError \/ Topic = modify(tc, _.copy(info = info))
}
