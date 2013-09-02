package chatless.services.clientApi


import chatless._
import chatless.model.Topic
import chatless.services._
import chatless.responses.TopicNotFoundError
import chatless.db.TopicDAO

trait TopicApi extends ServiceBase {

  val topicDao: TopicDAO

  private def canRead(cid: UserId, topic: Topic): Boolean =
    (  topic.public
    || (topic.op == cid)
    || (topic.sops contains cid)
    || (topic.participating contains cid)
    )

  private def fieldsFor(cid: UserId, topic: Topic): Set[String] = {
    import Topic._
    TID :: TITLE :: PUBLIC :: {
      if (canRead(cid, topic))
        INFO :: OP :: SOPS :: PARTICIPATING :: TAGS :: Nil
      else Nil
    }
  }.toSet

  /*
  private def getTopicInfo(cid: UserId, topic: Topic) = get {
    val topicMapped = fieldsFor(cid, topic)
    val topicSets = for {
      (k, v)  <- topicMapped
      cV <- v.cast[Set[String]]
    } yield k -> cV

    path(PathEnd) {
      resJson { complete { topicMapped } }
    } ~ path(topicMapped / PathEnd) { f: Any =>
      complete { f }
    } ~ path(topicSets / Segment / PathEnd) { (set: Set[String], v: String) =>
      complete { set contains v }
    }
  }*/

  private def infoRoute(cid: UserId, tid: TopicId) = get {
    val topic = topicDao get tid getOrElse { throw TopicNotFoundError(tid) }
    path(PathEnd) {
      resJson {
        complete {
          topic getFields fieldsFor(cid, topic)
        }
      }
    } ~ resText {
      path(Topic.TID) {
        complete(topic.tid / PathEnd)
      } ~ path(Topic.TITLE / PathEnd) {
        complete(topic.title)
      } ~ path(Topic.PUBLIC / PathEnd) {
        complete(topic.public)
      }
    } ~ authorize(canRead(cid, topic)) {
      path(Topic.INFO / PathEnd) {
        resJson { complete { topic.info } }
      } ~ path(Topic.OP / PathEnd)  {
        resText { complete { topic.op } }
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
      path(Topic.SOPS / Segment / PathEnd) { uid: UserId =>
        cup { PromoteSop(uid) }
      }
    } ~ delete {
      path(Topic.SOPS / Segment / PathEnd) { uid: UserId =>
        cup { DemoteSop(uid) }
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
    infoRoute(cid, tid) /*~ update(cid, topic)*/
  }

}