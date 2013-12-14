package chatless.db

import scalaz.\/

trait CounterDAO {
  def ensure(k: String): String \/ Option[Long]

  def next(k:String): String \/ Long
}
