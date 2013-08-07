package chatless.services

import spray.routing._

import shapeless._

import chatless._
import chatless.op2._
import argonaut._
import Argonaut._
import chatless.models.TopicM
import chatless.db.DatabaseAccessor
import akka.actor.ActorRefFactory
import scala.concurrent.ExecutionContext


class TopicApi(val dbac: DatabaseAccessor)(implicit val actorRefFactory: ActorRefFactory)
  extends CallerRoute with ServiceBase {

  val TOPIC_API_BASE = "topic"

  def canRead(cid: UserId, topic: TopicM): Boolean =
    (  topic.public
    || (topic.op == cid)
    || (topic.sops contains cid)
    || (topic.participating contains cid)
    )

  def fieldsFor(cid: UserId, topic: TopicM): List[String] = {
    import TopicM.{TID, TITLE, PUBLIC, INFO, OP, SOPS, PARTICIPATING, TAGS}
    if (canRead(cid, topic))
      TID :: TITLE :: PUBLIC :: INFO :: OP :: SOPS :: PARTICIPATING :: TAGS :: Nil
    else
      TID :: TITLE :: PUBLIC :: Nil
  }

  def getTopicInfo(cid: UserId)(topic: TopicM) =
    path(PathEnd) {
      completeJson { filterJson(topic.asJson, fieldsFor(cid, topic)) }
    } ~ path(TopicM.TID / PathEnd) {
      completeString(topic.tid)
    } ~ path(TopicM.TITLE / PathEnd) {
      completeString(topic.title)
    } ~ path(TopicM.PUBLIC / PathEnd) {
      completeBoolean(topic.public)
    } ~ authorize(canRead(cid, topic)) {
      path(TopicM.INFO / PathEnd) {
        completeJson(topic.info)
      } ~ path(TopicM.OP / PathEnd) {
        completeString(topic.op)
      } ~ path(TopicM.SOPS / PathEnd) {
        completeJson(topic.sops)
      } ~ path(TopicM.PARTICIPATING / PathEnd) {
        completeJson(topic.participating)
      } ~ path(TopicM.TAGS / PathEnd) {
        completeJson(topic.tags)
      }
    }

  def completeUpdateTopic(cid: UserId, topic: TopicM)(op: UpdateSpec with ForTopics): Route =
    onSuccess(dbac.updateTopic(cid, topic.tid, op)) { completeBoolean }

  def sopLevelUpdates(cup: UpdateSpec with ForTopics => Route): Route =
    put {
      path(TopicM.TITLE / PathEnd) {
        dEntity(as[String]) { v: String => cup { ChangeTitle(v) } }
      } ~ path(TopicM.PUBLIC / PathEnd) {
        dEntity(as[Boolean]) { v => cup { SetPublic(v) } }
      } ~ path(TopicM.INFO / PathEnd) {
        dEntity(as[Json]) { v => cup { UpdateInfo(v) } }
      } ~ path(TopicM.PARTICIPATING / Segment / PathEnd) { u: UserId =>
        optionJsonEntity { oj => cup { InviteUser(u, oj) } }
      } ~ path(TopicM.TAGS / Segment / PathEnd) { s: String =>
        cup { AddTag(s) }
      }
    } ~ delete {
      path(TopicM.PARTICIPATING / Segment / PathEnd) { u: UserId =>
        cup { KickUser(u) }
      } ~ path(TopicM.TAGS / Segment / PathEnd) { s: String =>
        cup { RemoveTag(s) }
      }
    }

  def opLevelUpdates(cup: UpdateSpec with ForTopics => Route): Route =
    put {
      path(TopicM.SOPS / Segment / PathEnd) { uid: UserId =>
        cup { PromoteSop(uid) }
      }
    } ~ delete {
      path(TopicM.SOPS / Segment / PathEnd) { uid: UserId =>
        cup { DemoteSop(uid) }
      }
    }

  def update(cid: UserId, topic: TopicM): Route = {
    val cup = completeUpdateTopic(cid, topic) _
    authorize(topic.op == cid || (topic.sops contains cid)) {
      sopLevelUpdates(cup)
    } ~ authorize(topic.op == cid) {
      opLevelUpdates(cup)
    }
  }

  def messageApi(cid: UserId, topic: TopicM): Route = {
    authorize((topic.op == cid) || (topic.sops contains cid) || (topic.participating contains cid)) {
      complete("")
    }
  }

  def apply(cid: UserId):Route = pathPrefix(TOPIC_API_BASE / Segment) { tid: TopicId =>
    onSuccess(dbac.getTopic(cid, tid)) { topic: TopicM =>
      get {
        getTopicInfo(cid)(topic)
      } ~ update(cid, topic) ~ messageApi(cid, topic)
    }
  }
}
