package chatless.services.clientApi


import chatless._
import chatless.models.{Topic, TopicDAO}
import chatless.services._
import shapeless._
import Typeable._
import chatless.db.TopicNotFoundError

trait TopicApi extends ServiceBase {

  val topicDao: TopicDAO

  private def canRead(cid: UserId, topic: Topic): Boolean =
    (  topic.public
    || (topic.op == cid)
    || (topic.sops contains cid)
    || (topic.participating contains cid)
    )

  private def fieldsFor(cid: UserId, topic: Topic): Map[String, Any] =  {
    (Topic.TID -> topic.tid) :: (Topic.TITLE -> topic.title) :: (Topic.PUBLIC -> topic.public) :: {
      if (canRead(cid, topic))
        (Topic.INFO -> topic.info) :: (Topic.OP -> topic.op) :: (Topic.SOPS -> topic.sops) ::
          (Topic.PARTICIPATING -> topic.participating) :: (Topic.TAGS -> topic.tags) :: Nil
      else Nil
    }
  }.toMap

  private def getTopicInfo(cid: UserId, topic: Topic) = get {
    val topicMapped = fieldsFor(cid, topic)
    val topicSets = for {
      (k, v)  <- topicMapped
      cV <- v.cast[Set[String]]
    } yield k -> cV

    resJson {
      path(PathEnd) {
        complete { topicMapped }
      } ~ path(topicMapped / PathEnd) { f: Any =>
        complete { mkRes(f) }
      } ~ path(topicSets / Segment / PathEnd) { (set: Set[String], v: String) =>
        complete { mkRes {set contains v} }
      }
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
    val topic = topicDao get tid getOrElse { throw TopicNotFoundError(tid) }
    getTopicInfo(cid, topic) /*~ update(cid, topic)*/
  }
}
