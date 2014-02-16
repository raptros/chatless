package chatless.services.clientApi

import chatless._
import chatless.services._
import chatless.services.routeutils._
import chatless.ops.TopicOps
import chatless.db.MessageDAO
import spray.http.StatusCodes
import spray.routing._
import org.json4s._
import chatless.model.{Topic, JDoc}

trait MessagesApi extends ServiceBase {
  val topicOps: TopicOps
  val messageDao: MessageDAO

  def queryMessages(tid: TopicId) = pathEndOrSingleSlash {
    complete { messageDao.first(tid, 1) }
  } ~ routeCarriers(
    "rstf"   carry getCount      buildQuery { messageDao.first(tid, _) },
    "last"   carry getCount      buildQuery { messageDao.last(tid, _) },
    "at"     carry getIdAndCount buildQuery { messageDao.at(tid, _, _) },
    "from"   carry getIdAndCount buildQuery { messageDao.from(tid, _, _) },
    "before" carry getIdAndCount buildQuery { messageDao.before(tid, _, _) },
    "after"  carry getIdAndCount buildQuery { messageDao.after(tid, _, _) }
  )

  private def checkAllowed(cid: UserId, topic: Topic): Directive0 =
    authorize(!topic.muted || topic.op == cid || (topic.sops contains cid) || (topic.voiced contains cid))

  private def extractJDoc: Directive1[JDoc] = entity(as[JObject]) map { jo => JDoc(jo.obj) }

  private def postCheck(cid: UserId, topic: Topic): Directive1[JDoc] = post & checkAllowed(cid, topic) & extractJDoc

  val messagesApi: CallerRoute = cid =>
    pathPrefix(TOPIC_API_BASE / Segment / MESSAGE_API_BASE) map topicOps.getOrThrow apply { topic =>
      authorize(topic.users contains cid) {
        get {
          queryMessages(topic.id)
        } ~ postCheck(cid, topic) { body =>
          completeCreation(X_CREATED_MESSAGE) {
            topicOps.sendMessage(cid, topic.id, body)
          }
        }
      }
    }
}
