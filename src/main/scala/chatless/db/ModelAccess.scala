package chatless.db
import chatless._
import argonaut._
import Argonaut._
import scalaz._
import scalaz.Validation._
import scalaz.std.list._
import scalaz.std.string._
import scalaz.syntax.bind._
import scalaz.syntax.validation._
import chatless.operation.{ValueContainer, GetField, OpRes}

trait ModelAccess { self =>
}
