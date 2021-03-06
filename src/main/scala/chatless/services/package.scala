package chatless

import scalaz.Semigroup
import spray.routing.{Prepender, PathMatcher, Route}
import spray.routing.RouteConcatenation._
import org.joda.time.DateTime
import spray.http.StatusCode
import chatless.responses.StateError
import shapeless.{HNil, HList}
import spray.http.Uri.Path

package object services {

  type CallerRoute = UserId => Route

  implicit def routeSemigroup: Semigroup[Route] = new Semigroup[Route] {
    def append(r1: Route, r2: => Route) = r1 ~ r2
  }

//  implicit def crSemigroup: Semigroup[CallerRoute]= new Semigroup[CallerRoute] {
//    def append(f1: CallerRoute, f2: => CallerRoute) = cid => f1(cid) ~ f2(cid)
//  }

  implicit class StateErrorCompleter(se: StateError) {
    import spray.routing.Directives
    import spray.http.MediaTypes._

    def complete(status: StatusCode) = Directives.respondWithMediaType(`text/plain`) {
      Directives.complete { status -> se.getMessage }
    }
  }

  val ME_API_BASE = "me"
  val TOPIC_API_BASE = "topic"
  val USER_API_BASE = "user"
  val MESSAGE_API_BASE = "message"
  val TAGGED_API_BASE = "tagged"
  val EVENT_API_BASE = "events"


//  implicit val StringCodecJson = CodecJson.derived[String]

}
