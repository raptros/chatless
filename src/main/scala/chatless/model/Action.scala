package chatless.model
import com.novus.salat.EnumStrategy
import com.novus.salat.annotations._

@EnumAs(strategy = EnumStrategy.BY_ID)
object Action extends Enumeration {
  val CREATE, REPLACE, ADD, REMOVE = Value
}
