package chatless.services.clientApi


import chatless._
import chatless.model.{JDoc, Topic}
import chatless.services._
import chatless.responses.{BoolR, StringR, TopicNotFoundError}
import chatless.db.{WriteStat, TopicDAO}
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import scalaz.syntax.std.boolean._
import spray.http.{StatusCodes, HttpResponse}
import spray.http.HttpHeaders.RawHeader
import spray.httpx.encoding.NoEncoding

trait TopicApi extends ServiceBase {

  val topicDao: TopicDAO

  private def canRead(cid: UserId, topic: Topic): Boolean =
    (  topic.public
    || (topic.op == cid)
    || (topic.sops contains cid)
    || (topic.participating contains cid)
    )

  private def fieldsFor(cid: UserId, topic: Topic): Set[String] = if (canRead(cid, topic))
    Topic.participantFields else Topic.publicFields

  private def fieldComplete[A <% JValue](field: String)(value: A) = path(field / PathEnd) {
    complete { Map(field -> value) }
  }

  private def infoRoute(cid: UserId, topic: Topic) = get {
    path(PathEnd) {
      complete {
        topic getFields fieldsFor(cid, topic)
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
    completeDBOp(topicDao.setTitle(tid, newTitle)) {
      log.info("topicApi: set title \"{}\" for topic {}", newTitle, tid)
    }
  }

  private def setPublic(cid: UserId, tid: TopicId, public: Boolean) = completeDBOp(topicDao.setPublic(tid, public)) {
    log.info("topicApi: set public to {} for topic {}", public, tid)
  }

  private def setMuted(cid: UserId, tid: TopicId, muted: Boolean) = completeDBOp(topicDao.setMuted(tid, muted)) {
    log.info("topicApi: set muted to {} for topic {}", muted, tid)
  }

  private def setInfo(cid: UserId, tid: TopicId, v: JObject) = completeDBOp(topicDao.setInfo(tid, JDoc(v.obj))) {
    log.info("topicApi: set info to \"{}\" for topic {}", compact(render(v)), tid)
  }

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
      path(Topic.TITLE / PathEnd) {
        entity(fromString[String]) {
          setTitle(cid, tid, _)
        }
      } ~ path(Topic.PUBLIC / PathEnd) {
        entity(fromString[Boolean]) {
          setPublic(cid, tid, _)
        }
      } ~ path(Topic.MUTED / PathEnd) {
        entity(fromString[Boolean]) {
          setMuted(cid, tid, _)
        }
      } ~ path(Topic.INFO / PathEnd) {
        entity(as[JObject]) {
          setInfo(cid, tid, _)
        }
      } ~ path(Topic.VOICED / Segment / PathEnd) {
        voiceUser(cid, tid, _)
      } ~ path(Topic.PARTICIPATING / Segment / PathEnd) {
        inviteUser(cid, tid, _)
      } ~ path(Topic.TAGS / Segment / PathEnd) {
        addTag(cid, tid, _)
      }
    } ~ delete {
      path(Topic.VOICED / Segment / PathEnd) {
        unvoiceUser(cid, tid, _)
      } ~ path(Topic.PARTICIPATING / Segment / PathEnd) {
        kickUser(cid, tid, _)
      } ~ path(Topic.TAGS / Segment / PathEnd) {
        removeTag(cid, tid, _)
      }
    }


  private def opLevelUpdates(cid: UserId, tid: TopicId) =
    put {
      path(Topic.SOPS / Segment / PathEnd) {
        promoteSop(cid, tid, _)
      }
    } ~ delete {
      path(Topic.SOPS / Segment / PathEnd) {
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
    resJson {
      infoRoute(cid, topic) ~ update(cid, topic)
    }
  }

}