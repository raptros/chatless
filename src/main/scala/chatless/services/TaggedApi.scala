package chatless.services

import chatless._
import spray.routing._
import shapeless._
import chatless.db.DatabaseAccessor
import akka.actor.ActorRefFactory

class TaggedApi(val dbac: DatabaseAccessor)(implicit val actorRefFactory: ActorRefFactory)
  extends CallerRoute with ServiceBase {

  val API_BASE = "tagged"

  def apply(cid: UserId): Route = pathPrefix(API_BASE) {
    complete("yo")
  }
}
