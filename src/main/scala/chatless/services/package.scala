package chatless

import scalaz.Semigroup
import spray.routing.Route
import spray.routing.RouteConcatenation._
import org.joda.time.DateTime
import spray.http.{MediaType, ContentTypeRange, HttpEntity, StatusCode}
import shapeless.HList

import scalaz.syntax.id._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import scalaz.std.option._

import spray.http.MediaTypes._
import chatless.model.{Coordinate, TopicCoordinate, User}
import spray.httpx.marshalling.Marshaller
import argonaut._
import Argonaut._
import spray.httpx.unmarshalling.{ContentExpected, SimpleUnmarshaller, MalformedContent, Unmarshaller}

package object services {

  type CallerRoute = UserId => Route

  implicit def routeSemigroup: Semigroup[Route] = new Semigroup[Route] {
    def append(r1: Route, r2: => Route) = r1 ~ r2
  }



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
