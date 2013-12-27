package chatless.services.clientApi

import chatless._
import chatless.model.{JDoc, Topic}
import chatless.services._
import chatless.services.routeutils._
import org.json4s._
import org.json4s.JsonDSL._
import spray.http.StatusCodes
import spray.httpx.encoding.NoEncoding
import chatless.ops.TopicOps
import chatless.model.inits.TopicInit
import spray.routing.{ConjunctionMagnet, Directive1}
import shapeless._

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

  private def query(cid: UserId, topic: Topic) =
    pathEndOrSingleSlash {
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
        Topic.TAGS   -> topic.tags
      )
    }

  private def sopLevelUpdates(cid: UserId, tid: TopicId) = put {
    sopLevelPuts(cid, tid)
  } ~ delete {
    sopLevelDeletes(cid, tid)
  }

  private def sopLevelDeletes(cid: UserId, tid: TopicId) = routeCarriers(
    Topic.USERS  carry segFin buildOp { topicOps.kickUser(cid, tid, _) },
    Topic.VOICED carry segFin buildOp { topicOps.removeVoiced(cid, tid, _) },
    Topic.BANNED carry segFin buildOp { topicOps.unbanUser(cid, tid, _) },
    Topic.TAGS   carry segFin buildOp { topicOps.removeTag(cid, tid, _) }
  )

  private def sopLevelPuts(cid: UserId, tid: TopicId) = routeCarriers(
    Topic.TITLE  carry sEntity(fromString[String])  buildOp { topicOps.setTitle(cid, tid, _) },
    Topic.PUBLIC carry sEntity(fromString[Boolean]) buildOp { topicOps.setPublic(cid, tid, _) },
    Topic.MUTED  carry sEntity(fromString[Boolean]) buildOp { topicOps.setMuted(cid, tid, _) },
    Topic.INFO   carry sEntity(as[JObject]) buildOp { j => topicOps.setInfo(cid, tid, JDoc(j.obj)) },
    //todo this next one needs the request system
    Topic.USERS  carry (segFin & optionJsonEntity) build { (uid, oj) =>
      complete(StatusCodes.NotImplemented)
    },
    Topic.VOICED carry segFin buildOp { topicOps.addVoiced(cid, tid, _) },
    Topic.BANNED carry segFin buildOp { topicOps.banUser(cid, tid, _) },
    Topic.TAGS   carry segFin buildOp { topicOps.addTag(cid, tid, _) }
  )

  private def update(cid: UserId, topic: Topic) = decodeRequest(NoEncoding) {
    put {
      authorize(topic.op == cid || (topic.sops contains cid)) {
        sopLevelPuts(cid, topic.id)
      } ~ authorize(topic.op == cid) {
        routeCarriers {
          Topic.SOPS carry segFin buildOp { topicOps.addSop(cid, topic.id, _) }
        }
      }
    } ~ delete {
      authorize(topic.op == cid || (topic.sops contains cid)) {
        sopLevelDeletes(cid, topic.id)
      } ~ authorize(topic.op == cid) {
        routeCarriers {
          Topic.SOPS carry segFin buildOp { topicOps.removeSop(cid, topic.id, _) }
        }
      }
    }
  }

  private val specTopic: Directive1[Topic] = pathPrefix(Segment) map topicOps.getOrThrow

  private lazy val postedInit: Directive1[TopicInit] = post & ConjunctionMagnet.fromDirective(entity(as[TopicInit]))

  val topicApi: CallerRoute = cid => pathPrefix(TOPIC_API_BASE) {
    specTopic { topic =>
      get { query(cid, topic) } ~ update(cid, topic)
    } ~ postedInit { ti =>
      completeCreation(X_CREATED_TOPIC) { topicOps.createTopic(cid, ti) }
    }
  }
}