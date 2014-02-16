package chatless.services

import spray.routing._
import shapeless.{HNil, HList, ::}
import chatless.db.WriteStat
import scalaz.syntax.id._
import akka.event.LoggingAdapter
import spray.routing


package object routeutils {

  /** The CarrierPath permits the construction of CarryBuilders using an infix notation.
    * @param path a path component to be associated with a carrier
    */
  implicit class CarrierPath(path: String) {

    /** @tparam L1 the types of the values that will be extracted by es
      * @param es a directive (possibly compound) that extracts values
      * @return a carry builder
      */
    def carry[L1 <: HList](es: => Directive[L1]) = new CarryBuilder[L1](path, es)
  }
}
