package chatless.services

import chatless._
import spray.routing._
import shapeless._

trait TaggedApi extends ServiceBase {
  val taggedApi = get {
    complete("yo")
  }
}
