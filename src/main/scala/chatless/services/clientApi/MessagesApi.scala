package chatless.services.clientApi

import chatless._
import chatless.op2._

import chatless.db.DatabaseAccessor
import akka.actor.ActorRefFactory

import spray.routing._

import argonaut._
import Argonaut._
import chatless.models.TopicM
import shapeless.HList
import chatless.services._
import chatless.op2.GetBefore
import chatless.op2.GetFirst
import chatless.op2.GetLast
import chatless.op2.GetAfter
import chatless.op2.GetAt
import chatless.op2.GetFrom
import chatless.op2.GetBefore
import chatless.op2.GetFirst
import chatless.op2.GetLast
import chatless.op2.GetAfter
import chatless.op2.GetAt
import chatless.op2.GetFrom
import spray.routing.PathMatchers.{IntNumber, PathEnd}
import com.google.inject.Inject

trait MessagesApi extends ServiceBase {

  private type GR = GetRelative with ForMessages

  private val getCount:Directive1[Int] = (path(PathEnd) & provide(1)) | path(IntNumber / PathEnd)

  private def conRel[L <: HList](cr: HListDeserializer[L, GR]):HListDeserializer[L, GR] = cr

  private val paths: Directive1[GR] =
    ( (path(PathEnd) & provide(GetFirst().asInstanceOf[GR]))
    | ((pathPrefix("first") & getCount) as conRel { GetFirst })
    | ((pathPrefix("last") & getCount) as conRel { GetLast })
    | ((pathPrefix("at" / Segment) & getCount) as conRel { GetAt })
    | ((pathPrefix("before" / Segment) & getCount) as conRel { GetBefore })
    | ((pathPrefix("from" / Segment) & getCount) as conRel { GetFrom })
    | ((pathPrefix("after" / Segment) & getCount) as conRel { GetAfter })
    )

  private def relRoute(cid: UserId, topic: TopicM) = paths { gr: GR =>
    completeJson { dbac.getMessages(cid, topic.tid, gr) }
  }

  val messagesApi: CallerRoute = cid => pathPrefix(TOPIC_API_BASE / Segment / MESSAGE_API_BASE) { tid: String =>
    onSuccess(dbac.getTopic(cid, tid)) { topic: TopicM =>
      authorize((topic.op == cid) || (topic.sops contains cid) || (topic.participating contains cid)) {
        get {
          relRoute(cid, topic)
        } ~ post {
          dEntity(as[Json]) { j: Json =>
            completeJson { dbac.createMessage(cid, topic.tid, j) }
          }
        }
      }
    }
  }
}
