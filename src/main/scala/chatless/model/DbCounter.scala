package chatless.model

import chatless._

case class DbCounter(
  id: String,
  c: Long)

object DbCounter {
  val ID = "id"
  val COUNTER = "c"
}
