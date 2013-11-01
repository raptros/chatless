package chatless.services.clientApi


import chatless._
import chatless.model.{JDoc, Topic}
import chatless.services._
import chatless.responses.TopicNotFoundError
import chatless.db.TopicDAO
import scalaz.std.string._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import spray.http.StatusCodes
import spray.httpx.encoding.NoEncoding
import chatless.ops.TopicOps

trait TopicApi extends ServiceBase {

  val topicOps: TopicOps

  private def canRead(cid: UserId, topic: Topic): Boolean =
    (  topic.public
    || (topic.op == cid)
    || (topic.sops contains cid)
    || (topic.users contains cid)
    )

  private def fieldsFor(cid: UserId, topic: Topic): Set[String] = if (canRead(cid, topic))
    Topic.userFields else Topic.publicFields

  private def infoRoute(cid: UserId, topic: Topic) = get {
    pathEnd {
      resJson { complete { topic getFields fieldsFor(cid, topic) } }
    } ~ completeFieldsAs(
      Topic.ID     -> topic.id,
      Topic.TITLE  -> topic.title,
      Topic.PUBLIC -> topic.public,
      Topic.MUTED  -> topic.muted
    ) ~ authorize(canRead(cid, topic)) {
      completeFieldsAs(
        Topic.INFO   -> topic.info,
        Topic.OP     -> topic.op,
        Topic.SOPS   -> topic.sops,
        Topic.VOICED -> topic.voiced,
        Topic.USERS  -> topic.users,
        Topic.BANNED -> topic.banned,
        Topic.TAGS   -> topic.tags
      ) ~ completeWithContains(
        Topic.SOPS   -> topic.sops,
        Topic.VOICED -> topic.voiced,
        Topic.USERS  -> topic.users,
        Topic.BANNED -> topic.banned,
        Topic.TAGS   -> topic.tags)
    }
  }

  private def setTitle(cid: UserId, tid: TopicId, newTitle: String) =
    validate(!newTitle.isEmpty, "invalid topic title") {
      completeOp { topicOps.setTitle(cid, tid, newTitle) }
    }

  private def setPublic(cid: UserId, tid: TopicId, public: Boolean) = completeOp { topicOps.setPublic(cid, tid, public) }

  private def setMuted(cid: UserId, tid: TopicId, muted: Boolean) = completeOp { topicOps.setMuted(cid, tid, muted) }

  private def setInfo(cid: UserId, tid: TopicId, v: JObject) = completeOp { topicOps.setInfo(cid, tid, JDoc(v.obj)) }

  private def voiceUser(cid: UserId, tid: TopicId, uid: UserId) = completeOp { topicOps.addVoiced(cid, tid, uid) }

  private def unvoiceUser(cid: UserId, tid: TopicId, uid: UserId) = completeOp { topicOps.removeVoiced(cid, tid, uid) }

  private def inviteUser(cid: UserId, tid: TopicId, uid: UserId) =
    optionJsonEntity { t =>
      complete {
        StatusCodes.NotImplemented
      }
    }

  private def kickUser(cid: UserId, tid: TopicId, uid: UserId) = completeOp { topicOps.kickUser(cid, tid, uid) }

  private def addTag(cid: UserId, tid: TopicId, tag: String) = completeOp { topicOps.addTag(cid, tid, tag) }

  private def removeTag(cid: UserId, tid: TopicId, tag: String) = completeOp { topicOps.removeTag(cid, tid, tag) }

  private def banUser(cid: UserId, tid: TopicId, uid: UserId) = completeOp { topicOps.banUser(cid, tid, uid) }

  private def unbanUser(cid: UserId, tid: TopicId, uid: UserId) = completeOp { topicOps.unbanUser(cid, tid, uid) }

  private def sopLevelUpdates(cid: UserId, tid: TopicId) =
    put {
      path(Topic.TITLE) {
        entity(fromString[String]) {
          setTitle(cid, tid, _)
        }
      } ~ path(Topic.PUBLIC) {
        entity(fromString[Boolean]) {
          setPublic(cid, tid, _)
        }
      } ~ path(Topic.MUTED) {
        entity(fromString[Boolean]) {
          setMuted(cid, tid, _)
        }
      } ~ path(Topic.INFO) {
        entity(as[JObject]) {
          setInfo(cid, tid, _)
        }
      } ~ path(Topic.VOICED / Segment) {
        voiceUser(cid, tid, _)
      } ~ path(Topic.USERS / Segment) {
        inviteUser(cid, tid, _)
      } ~ path(Topic.TAGS / Segment) {
        addTag(cid, tid, _)
      }
    } ~ delete {
      path(Topic.VOICED / Segment) {
        unvoiceUser(cid, tid, _)
      } ~ path(Topic.USERS / Segment) {
        kickUser(cid, tid, _)
      } ~ path(Topic.TAGS / Segment) {
        removeTag(cid, tid, _)
      }
    }

  private def promoteSop(cid: UserId, tid: TopicId, uid: UserId) = completeOp { topicOps.addSop(cid, tid, uid) }

  private def demoteSop(cid: UserId, tid: TopicId, uid: UserId) = completeOp { topicOps.removeSop(cid, tid, uid) }

  private def opLevelUpdates(cid: UserId, tid: TopicId) =
    put {
      path(Topic.SOPS / Segment) {
        promoteSop(cid, tid, _)
      }
    } ~ delete {
      path(Topic.SOPS / Segment) {
        demoteSop(cid, tid, _)
      }
    }

  private def update(cid: UserId, topic: Topic) = decodeRequest(NoEncoding) {
    authorize(topic.op == cid || (topic.sops contains cid)) {
      sopLevelUpdates(cid, topic.id)
    } ~ authorize(topic.op == cid) {
      opLevelUpdates(cid, topic.id)
    }
  }

  val topicApi: CallerRoute = cid => pathPrefix(TOPIC_API_BASE / Segment) map topicOps.getOrThrow apply { topic =>
    infoRoute(cid, topic) ~ update(cid, topic)
  }

}