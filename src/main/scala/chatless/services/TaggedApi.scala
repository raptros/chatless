package chatless.services

import chatless._
import spray.routing._
import shapeless._
import chatless.operation.{ResTagged, OpRes, OpSpec}

trait TaggedApi extends ServiceBase with SpecDirectives {
  val tagged:Directive[UserId :: OpRes :: HNil] = (userAuth & pathPrefix("tagged" / Segment)) hmap {
    case cid :: tag :: HNil => cid :: ResTagged(tag).asInstanceOf[OpRes] :: HNil
  }

  val browseTag:Directive1[OpSpec] = Relative.default(false) |
    Relative.last |
    Relative.at |
    Relative.before |
    Relative.from |
    Relative.after

  val taggedApi:DOperation = (get & tagged & browseTag) as { operation }
}
