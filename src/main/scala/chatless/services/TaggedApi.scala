package chatless.services

import chatless._
import spray.routing._
import shapeless._
import chatless.operation.{ResTagged, OpRes, OpSpec}

trait TaggedApi extends ServiceBase {
  val taggedApi = get {
    complete("yo")
  }
}
