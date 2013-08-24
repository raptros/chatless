package chatless.db.handlers
import chatless._

import chatless.op2._

import chatless.db.daos._
import com.google.inject.Inject
import chatless.db.TopicNotFoundError

class MongoTopicHandler @Inject() (val topicDao: TopicDAO)
  extends TopicOpHandler {

  def apply(cid: UserId, tid: TopicId, spec: Specifier with ForTopics): Any = spec match {
    case GetRes => topicDao get tid getOrElse { throw TopicNotFoundError(tid, cid) }
    case SetPublic(public) =>
    case UpdateInfo(info) =>
    case BlockUser(user) =>
    case UnblockUser(user) =>
    case AddTag(tag) =>
    case RemoveTag(tag) =>
    case ChangeTitle(title) =>
    case InviteUser(uid, additional) =>
    case KickUser(uid) =>
    case PromoteSop(uid) =>
    case DemoteSop(uid) =>
  }
}
