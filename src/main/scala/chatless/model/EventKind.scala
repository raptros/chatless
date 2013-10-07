package chatless.model
import com.novus.salat.EnumStrategy
import com.novus.salat.annotations._

@EnumAs(strategy = EnumStrategy.BY_ID)
object EventKind extends Enumeration {
  val USER_UPDATE, TOPIC_UPDATE, MESSAGE, REQUEST = Value
}
