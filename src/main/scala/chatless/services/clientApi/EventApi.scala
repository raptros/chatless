package chatless.services.clientApi

import chatless.op2._

import chatless.UserId
import spray.routing._

import shapeless._
import chatless.db.DatabaseAccessor
import akka.actor.ActorRefFactory
import chatless.services._
import chatless.op2.GetBefore
import chatless.op2.GetAfter
import chatless.op2.GetLast
import chatless.op2.GetAt
import chatless.op2.GetFrom
import chatless.op2.GetBefore
import chatless.op2.GetAfter
import chatless.op2.GetLast
import chatless.op2.GetAt
import chatless.op2.GetFrom
import spray.routing.PathMatchers.{IntNumber, PathEnd}
import com.google.inject.Inject

trait EventApi extends ServiceBase {

  private type GR = GetRelative with ForEvents

  private val getCount:Directive1[Int] = (path(PathEnd) & provide(1)) | path(IntNumber / PathEnd)

  private def conRel[L <: HList](cr: HListDeserializer[L, GR]):HListDeserializer[L, GR] = cr

  private val paths: Directive1[GR] =
    ( (path(PathEnd) & provide(GetLast().asInstanceOf[GR]))
      | ((pathPrefix("last") & getCount) as conRel { GetLast })
      | ((pathPrefix("at" / Segment) & getCount) as conRel { GetAt })
      | ((pathPrefix("before" / Segment) & getCount) as conRel { GetBefore })
      | ((pathPrefix("from" / Segment) & getCount) as conRel { GetFrom })
      | ((pathPrefix("after" / Segment) & getCount) as conRel { GetAfter })
      )

  val eventApi: CallerRoute = cid => pathPrefix(EVENT_API_BASE) {
    get {
      paths { gr: GR =>
        completeJson { dbac.getEvents(cid, gr) }
      }
    }
  }
}
