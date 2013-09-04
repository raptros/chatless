package chatless.services.clientApi


import chatless._
import chatless.model.Topic
import chatless.services._
import chatless.responses.{BoolR, StringR, TopicNotFoundError}
import chatless.db.TopicDAO
import org.json4s._
import org.json4s.JsonDSL._

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

  private def getTopic(tid: TopicId) = topicDao get tid getOrElse { throw TopicNotFoundError(tid) }

  private def infoRoute(cid: UserId, tid: TopicId) = get {
    val topic = topicDao get tid getOrElse { throw TopicNotFoundError(tid) }
    path(PathEnd) {
      complete {
        topic getFields fieldsFor(cid, topic)
      }
    } ~ path(Topic.ID / PathEnd) {
      complete { Topic.ID -> topic.id }
    } ~ path(Topic.TITLE / PathEnd) {
      complete { Topic.TITLE -> topic.title }
    } ~ path(Topic.PUBLIC / PathEnd) {
      complete { Topic.PUBLIC -> topic.public }
    } ~ authorize(canRead(cid, topic)) {
      path(Topic.INFO / PathEnd) {
        complete { topic.info }
      } ~ path(Topic.OP / PathEnd)  {
        complete { StringR(topic.op) }
      } ~ path(Topic.SOPS / PathEnd) {
        resJson { complete { topic.sops } }
      } ~ path(Topic.PARTICIPATING / PathEnd) {
        resJson { complete { topic.participating } }
      } ~ path(Topic.TAGS / PathEnd) {
        resJson { complete { topic.tags } }
      } ~ setCompletion(
        Topic.SOPS -> topic.sops,
        Topic.PARTICIPATING -> topic.participating,
        Topic.TAGS -> topic.tags)
    }
  }

/*  private def sopLevelUpdates(cup: UpdateSpec with ForTopics => Route): Route =
    put {
      path(Topic.TITLE / PathEnd) {
        dEntity(as[String]) { v: String => cup { ChangeTitle(v) } }
      } ~ path(Topic.PUBLIC / PathEnd) {
        dEntity(as[Boolean]) { v => cup { SetPublic(v) } }
      } ~ path(Topic.INFO / PathEnd) {
        dEntity(as[Json]) { v => cup { UpdateInfo(v) } }
      } ~ path(Topic.PARTICIPATING / Segment / PathEnd) { u: UserId =>
        optionJsonEntity { oj => cup { InviteUser(u, oj) } }
      } ~ path(Topic.TAGS / Segment / PathEnd) { s: String =>
        cup { AddTag(s) }
      }
    } ~ delete {
      path(Topic.PARTICIPATING / Segment / PathEnd) { u: UserId =>
        cup { KickUser(u) }
      } ~ path(Topic.TAGS / Segment / PathEnd) { s: String =>
        cup { RemoveTag(s) }
      }
    }

  private def opLevelUpdates(cup: UpdateSpec with ForTopics => Route): Route =
    put {
      path(Topic.SOPS / Segment / PathEnd) { id: UserId =>
        cup { PromoteSop(id) }
      }
    } ~ delete {
      path(Topic.SOPS / Segment / PathEnd) { id: UserId =>
        cup { DemoteSop(id) }
      }
    }

  private def update(cid: UserId, topic: Topic): Route = {
    val cup = completeUpdateTopic(cid, topic) _
    authorize(topic.op == cid || (topic.sops contains cid)) {
      sopLevelUpdates(cup)
    } ~ authorize(topic.op == cid) {
      opLevelUpdates(cup)
    }
  }*/

  val topicApi: CallerRoute = cid => pathPrefix(TOPIC_API_BASE / Segment) { tid: TopicId =>
    resJson {
      infoRoute(cid, tid) /*~ update(cid, topic)*/
    }
  }

}