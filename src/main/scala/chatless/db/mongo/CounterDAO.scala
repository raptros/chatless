package chatless.db.mongo

import chatless.model.{Coordinate, TopicCoordinate}
import chatless.db.DbError
import scalaz.\/

trait CounterDAO {
  /** returns the value of a counter for messages in a topic
    * @param purpose a short simple string to scope this counter separately from the coordinate
    * @param coordinate a coordinate to have a counter for
    * @return the number of times the counter has been incremented (the first call will get 1), or failure
    */
  def inc(purpose: String, coordinate: Coordinate): DbError \/ Long
}
