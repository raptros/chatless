package chatless.events.model

import chatless._
import com.novus.salat.annotations._
import com.novus.salat.EnumStrategy

@EnumAs(strategy = EnumStrategy.BY_VALUE)
object Action extends Enumeration {
  val Create = Value("create")
  val Replace = Value("replace")
  val Add = Value("add")
  val Remove = Value("remove")
}
