package chatless

import scalaz._
import chatless.responses.StateError

package object db {
  type ValidModel[A] = StateError \/ A

  type WriteStat = String \/ Boolean

  val EVENT_SEQUENCE_ID = "eventSequence"
}
