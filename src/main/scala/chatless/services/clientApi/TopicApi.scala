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

  val topicDao: TopicDAO

  private def canRead(cid: UserId, topic: Topic): Boolean =
    (  topic.public
    || (topic.op == cid)
    || (topic.sops contains cid)
    || (topic.participating contains cid)
    )

  private def fieldsFor(cid: UserId, topic: Topic): Set[String] = if (canRead(cid, topic))
    Topic.participantFields else Topic.publicFields

  private def fieldComplete[A <% JValue](field: String)(value: A) = path(field) {
    resJson { complete { Map(field -> value) } }
  }

  private def infoRoute(cid: UserId, topic: Topic) = get {
    pathEnd {
      resJson {
        complete {
          topic getFields fieldsFor(cid, topic)
        }
      }
    } ~ fieldComplete(Topic.ID) {
      topic.id
    } ~ fieldComplete(Topic.TITLE) {
      topic.title
    } ~ fieldComplete(Topic.PUBLIC) {
      topic.public
    } ~ fieldComplete(Topic.MUTED) {
      topic.muted
    } ~ authorize(canRead(cid, topic)) {
      fieldComplete(Topic.INFO) {
        topic.info
      } ~ fieldComplete(Topic.OP)  {
        topic.op
      } ~ fieldComplete(Topic.SOPS) {
        topic.sops
      } ~ fieldComplete(Topic.VOICED) {
        topic.voiced
      } ~ fieldComplete(Topic.PARTICIPATING) {
        topic.participating
      } ~ fieldComplete(Topic.BANNED) {
        topic.banned
      } ~ fieldComplete(Topic.TAGS) {
        topic.tags
      } ~ setCompletion(
        Topic.SOPS -> topic.sops,
        Topic.VOICED -> topic.voiced,
        Topic.PARTICIPATING -> topic.participating,
        Topic.BANNED -> topic.banned,
        Topic.TAGS -> topic.tags)
    }
  }

  private def setTitle(cid: UserId, tid: TopicId, newTitle: String) = validate(!newTitle.isEmpty, "invalid topic title") {
    completeOp { topicOps.setTitle(cid, tid, newTitle) }
  }

  private def setPublic(cid: UserId, tid: TopicId, public: Boolean) = completeOp { topicOps.setPublic(cid, tid, public) }

  private def setMuted(cid: UserId, tid: TopicId, muted: Boolean) = completeOp { topicOps.setMuted(cid, tid, muted) }

  private def setInfo(cid: UserId, tid: TopicId, v: JObject) = completeOp { topicOps.setInfo(cid, tid, JDoc(v.obj)) }

  private def voiceUser(cid: UserId, tid: TopicId, uid: UserId) = complete { StatusCodes.NotImplemented }

  private def unvoiceUser(cid: UserId, tid: TopicId, uid: UserId) = complete { StatusCodes.NotImplemented }

  private def inviteUser(cid: UserId, tid: TopicId, uid: UserId) =
    optionJsonEntity { t =>
      complete {
        StatusCodes.NotImplemented
      }
    }

  private def kickUser(cid: UserId, tid: TopicId, uid: UserId) = complete {
    StatusCodes.NotImplemented
  }

  private def addTag(cid: UserId, tid: TopicId, tag: String) = completeDBOp(topicDao.addTag(tid, tag)) {
    log.info("topicApi: added tag \"{}\" to topic {}", tag, tid)
  }

  private def removeTag(cid: UserId, tid: TopicId, tag: String) = completeDBOp(topicDao.removeTag(tid, tag)) {
    log.info("topicApi: removed tag \"{}\" to topic {}", tag, tid)
  }

  private def promoteSop(cid: UserId, tid: TopicId, uid: UserId) = complete { StatusCodes.NotImplemented }

  private def demoteSop(cid: UserId, tid: TopicId, uid: UserId) = complete { StatusCodes.NotImplemented }

  private def banUser(cid: UserId, tid: TopicId, uid: UserId) = complete { StatusCodes.NotImplemented }

  private def unbanUser(cid: UserId, tid: TopicId, uid: UserId) = complete { StatusCodes.NotImplemented }

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
      } ~ path(Topic.PARTICIPATING / Segment) {
        inviteUser(cid, tid, _)
      } ~ path(Topic.TAGS / Segment) {
        addTag(cid, tid, _)
      }
    } ~ delete {
      path(Topic.VOICED / Segment) {
        unvoiceUser(cid, tid, _)
      } ~ path(Topic.PARTICIPATING / Segment) {
        kickUser(cid, tid, _)
      } ~ path(Topic.TAGS / Segment) {
        removeTag(cid, tid, _)
      }
    }


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

  val topicApi: CallerRoute = cid => pathPrefix(TOPIC_API_BASE / Segment) { tid: TopicId =>
    val topic = topicDao get tid getOrElse { throw TopicNotFoundError(tid) }
    infoRoute(cid, topic) ~ update(cid, topic)
  }

}