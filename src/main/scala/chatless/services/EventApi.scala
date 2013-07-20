package chatless.services

import chatless.UserId
import chatless.operation.{OpSpec, ResEvents, OpRes}

import spray.routing._

import shapeless._

trait EventApi extends ServiceBase with SpecDirectives {
  val events:Directive[UserId :: OpRes :: HNil] = (userAuth & pathPrefix("event")) map { cid:UserId =>
    cid :: ResEvents.asInstanceOf[OpRes] :: HNil
  }

  val getEvent:Directive1[OpSpec] = Relative.default(false) |
    Relative.last |
    Relative.at |
    Relative.before |
    Relative.from |
    Relative.after

  val eventApi:DOperation = (get & events & getEvent) as { operation }

}
