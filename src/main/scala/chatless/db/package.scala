package chatless

import scalaz._
import chatless.responses.StateError

package object db {
  type ValidModel[A] = StateError \/ A

}
