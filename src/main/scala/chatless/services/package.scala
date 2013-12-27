package chatless

import scalaz.Semigroup
import spray.routing.{Prepender, PathMatcher, Route}
import spray.routing.RouteConcatenation._
import org.joda.time.DateTime
import spray.http.StatusCode
import chatless.responses.StateError
import shapeless.{HNil, HList}
import spray.http.Uri.Path

import scalaz.syntax.id._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import scalaz.std.list._
import scalaz.std.option._
import scalaz.std.string._

import org.json4s._
import org.json4s.JsonDSL._

package object services {

  type CallerRoute = UserId => Route

  implicit def routeSemigroup: Semigroup[Route] = new Semigroup[Route] {
    def append(r1: Route, r2: => Route) = r1 ~ r2
  }

  implicit class StateErrorCompleter(se: StateError) {
    import spray.routing.Directives
    import spray.http.MediaTypes._

    def complete(status: StatusCode) = Directives.respondWithMediaType(`text/plain`) {
      Directives.complete { status -> se.getMessage }
    }
  }

  private def stackTrace2StringList(st: Array[StackTraceElement]) = {
    val stList = (st ?? Array()).toList map { _.toString }
    stList.nonEmpty ?? stList.some
  }

  /** a tidy way to produce a json representation of any Throwable. */
  def throwableToJson(t: Throwable): JObject = JObject() ~
    ("type"    -> t.getClass.toString) ~
    ("message" -> Option(t.getMessage)) ~
    ("trace"   -> stackTrace2StringList(t.getStackTrace)) ~
    ("cause"   -> (Option(t.getCause) map { throwableToJson }))

  val ME_API_BASE = "me"
  val TOPIC_API_BASE = "topic"
  val USER_API_BASE = "user"
  val MESSAGE_API_BASE = "message"
  val TAGGED_API_BASE = "tagged"
  val EVENT_API_BASE = "events"

  def Header(name: String): String = s"x-chatless-$name"

  val X_UPDATED = Header("updated")
  val X_CREATED_TOPIC = Header("created-topic")
  val X_CREATED_MESSAGE = Header("created-message")

}
