package chatless.services.routeutils

import shapeless._
import spray.routing._

/** A CompleterCarrier is a pair of an extraction directive and a function that produces a route with the extracted
  * values. The route method performs the operation that produces this route.
  * @see HelperDirectives for a method that uses CompleterCarriers to map paths to routes.
  */
trait CompleterCarrier { self =>
  type L <: HList

  val extractions: Directive[L]

  val completer: L => Route

  def route = extractions happly completer
}
