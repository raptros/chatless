package chatless.services

import chatless.UserId
import spray.routing._

import shapeless._
import chatless.db.DatabaseAccessor
import akka.actor.ActorRefFactory

class EventApi(val dbac: DatabaseAccessor)(implicit val actorRefFactory: ActorRefFactory)
  extends CallerRoute with ServiceBase {

  val API_BASE = "events"

  def apply(cid: UserId): Route = pathPrefix(API_BASE) {
    complete("yo from events")
  }
}
