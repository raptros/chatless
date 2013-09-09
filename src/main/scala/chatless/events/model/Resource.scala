package chatless.events.model

import chatless._
import com.novus.salat.annotations._
import com.novus.salat.EnumStrategy

@EnumAs(strategy = EnumStrategy.BY_VALUE)
object Resource extends Enumeration {
  val User = Value("user")
  val Topcic = Value("topic")
  val Message = Value("message")
}
