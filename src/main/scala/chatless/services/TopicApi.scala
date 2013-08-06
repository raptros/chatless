package chatless.services

import spray.routing._

import shapeless._

import chatless._
import chatless.op2._
import argonaut._


trait TopicApi extends ServiceBase {
  val TOPIC_API_BASE = "topic"

  def getTopicInfo(cid:UserId)(topic:TopicM) =
    path(PathEnd) {
      completeJson(topic)
    } ~ path(TopicM.TID / PathEnd) {
      completeString(topic.tid)
    } ~ path(TopicM.TITLE / PathEnd) {
      completeString(topic.title)
    } ~ path(TopicM.PUBLIC / PathEnd) {
      completeBoolean(topic.public)
    } ~ authorize(topic.public || (topic.op == cid) || (topic.sops contains cid) || (topic.participating contains cid)) {
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

  def completeUpdateTopic(cid:UserId, topic:TopicM)(op: UpdateSpec with ForTopics):Route =
    onSuccess(dbac.updateTopic(cid, topic.tid, op)) { completeBoolean }

  def sopLevelUpdates(cup:UpdateSpec with ForTopics => Route):Route =
    put {
      path(TopicM.TITLE / PathEnd) {
        dEntity(as[String]) { v:String => cup { ChangeTitle(v) } }
      } ~ path(TopicM.PUBLIC / PathEnd) {
        dEntity(as[Boolean]) { v => cup { SetPublic(v) } }
      } ~ path(TopicM.INFO / PathEnd) {
        dEntity(as[Json]) { v => cup { UpdateInfo(v) } }
      } ~ path(TopicM.PARTICIPATING / Segment / PathEnd) { u:UserId =>
        optionJsonEntity { oj => cup { InviteUser(u, oj) } }
      }
    }

  def update(cid:UserId, topic:TopicM):Route = {
    val cup = completeUpdateTopic(cid, topic)
    authorize(topic.op == cid || (topic.sops contains cid)) {
      sopLevelUpdates(cup)
    }
  }


  def topicApi(cid:UserId) = pathPrefix(TOPIC_API_BASE / Segment) { tid:TopicId =>
    onSuccess(dbac.getTopic(cid, tid)) { topic:TopicM =>
      get {
        getTopicInfo(cid)(topic)
      }
    }
  }
}
