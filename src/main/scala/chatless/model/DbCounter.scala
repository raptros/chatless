package chatless.model

import chatless._
import com.novus.salat.annotations._

case class DbCounter(
  @Key("_id") id: String,
  c: Long)

object DbCounter {
  val ID = "id"
  val COUNTER = "c"
}
