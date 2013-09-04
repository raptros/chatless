package chatless.services.clientApi

import chatless.op2._


import chatless.services._

trait MessagesApi extends ServiceBase {

//  val topicDao: TopicDAO
//
//  def getUser(cid: UserId): Directive1[User] = provide((userDao get cid) getOrElse { throw UserNotFoundError(cid) })

  private type GR = GetRelative with ForMessages

  //private val getCount:Directive1[Int] = (path(PathEnd) & provide(1)) | path(IntNumber / PathEnd)

 // private def conRel[L <: HList](cr: HListDeserializer[L, GR]):HListDeserializer[L, GR] = cr

  /*
  private val pathMap: Directive1[GR] =
    ( (path(PathEnd) & provide(GetFirst().asInstanceOf[GR]))
    | ((pathPrefix("first") & getCount) as conRel { GetFirst })
    | ((pathPrefix("last") & getCount) as conRel { GetLast })
    | ((pathPrefix("at" / Segment) & getCount) as conRel { GetAt })
    | ((pathPrefix("before" / Segment) & getCount) as conRel { GetBefore })
    | ((pathPrefix("from" / Segment) & getCount) as conRel { GetFrom })
    | ((pathPrefix("after" / Segment) & getCount) as conRel { GetAfter })
    )
*/
  /*
  private def relRoute(cid: UserId, topic: Topic) = pathMap { gr: GR =>
    resJson {
      complete {
        "no"
//        dbac.getMessages(cid, topic.id, gr)
      }
    }
  }*/

  val messagesApi: CallerRoute = cid => pathPrefix(TOPIC_API_BASE / Segment / MESSAGE_API_BASE) { tid: String =>
      /*authorize((topic.op == cid) || (topic.sops contains cid) || (topic.participating contains cid)) {
        get {
          relRoute(cid, topic)
        } ~ post {
          dEntity(as[Json]) { j: Json =>
            completeJson { dbac.createMessage(cid, topic.id, j) }
          }
        }
      }
    }*/
    complete { "no " }
  }
}
