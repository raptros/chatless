package chatless

import scalaz.Semigroup
import spray.routing.Route
import spray.routing.RouteConcatenation._
import argonaut.CodecJson

package object services {

  type CallerRoute = UserId => Route

  implicit def routeSemigroup: Semigroup[Route] = new Semigroup[Route] {
    def append(r1: Route, r2: => Route) = r1 ~ r2
  }

  implicit val StringCodecJson = CodecJson.derived[String]

}
