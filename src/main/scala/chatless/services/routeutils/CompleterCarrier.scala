package chatless.services.routeutils

import shapeless._
import spray.routing._

trait CompleterCarrier { self =>
  type L <: HList
  val extractions: Directive[L]
  val completer: L => Route
  def route = extractions happly completer
}
