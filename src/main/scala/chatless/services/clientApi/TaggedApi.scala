package chatless.services.clientApi

import chatless._
import spray.routing._
import shapeless._
import chatless.db.DatabaseAccessor
import akka.actor.ActorRefFactory
import chatless.services._
import com.google.inject.Inject

trait TaggedApi extends ServiceBase {

  val taggedApi: CallerRoute = cid => pathPrefix(TAGGED_API_BASE) {
    complete("yo")
  }
}
