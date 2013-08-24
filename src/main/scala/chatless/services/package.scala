package chatless

import scalaz.Semigroup
import spray.routing.Route
import spray.routing.RouteConcatenation._
import argonaut._
import Argonaut._
import org.joda.time.DateTime

package object services {

  type CallerRoute = UserId => Route

  implicit def routeSemigroup: Semigroup[Route] = new Semigroup[Route] {
    def append(r1: Route, r2: => Route) = r1 ~ r2
  }


  val ME_API_BASE = "me"
  val TOPIC_API_BASE = "topic"
  val MESSAGE_API_BASE = "message"
  val TAGGED_API_BASE = "tagged"
  val EVENT_API_BASE = "events"


  implicit val StringCodecJson = CodecJson.derived[String]

}
