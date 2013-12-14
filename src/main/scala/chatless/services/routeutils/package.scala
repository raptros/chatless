package chatless.services

import spray.routing._
import shapeless.{HNil, HList, ::}
import chatless.db.WriteStat
import scalaz.syntax.id._
import akka.event.LoggingAdapter
import spray.routing


package object routeutils {
  implicit class CarrierPath(path: String) {
    def carry[L1 <: HList](es: Directive[L1]) = new CarryBuilder[L1](path, es)
  }
}
