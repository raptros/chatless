package chatless.db.mongo

import chatless.model.TopicCoordinate
import chatless.db.DbError
import scalaz.\/

trait MessageCounterDAO {
  /** returns the value of a counter for messages in a topic
    * @param topic a topic coordinate to have a counter for
    * @return the number of times the counter has been incremented (the first call will get 1), or failure
    */
  def inc(topic: TopicCoordinate): DbError \/ Long
}
