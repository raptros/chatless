package chatless


package object db {
  import argonaut._

  import Argonaut._

  import chatless.operation.{JsonVC, StringVC, BooleanVC, OpRes}
  import scalaz._
  import scalaz.Validation

  val jAddF: (Json, (String, Json)) => Json = _.->:(_)

  type CallerCan = UserId => Boolean

  type CallerRes = UserId => OpRes


  type StateValidJson = StateError ValidationNel Json
}
