package chatless.services.clientApi

import spray.routing._


import shapeless._

import chatless._
import chatless.op2._
import argonaut._
import Argonaut._
import chatless.models.{TypedField, TopicM}
import chatless.db.DatabaseAccessor
import akka.actor.ActorRefFactory
import scala.concurrent.ExecutionContext
import chatless.services._
import chatless.op2.KickUser
import chatless.op2.DemoteSop
import chatless.op2.PromoteSop
import chatless.op2.ChangeTitle
import shapeless.::
import chatless.op2.UpdateInfo
import chatless.op2.InviteUser
import chatless.op2.RemoveTag
import chatless.op2.SetPublic
import chatless.op2.AddTag
import chatless.op2.KickUser
import chatless.op2.DemoteSop
import chatless.op2.PromoteSop
import chatless.op2.ChangeTitle
import shapeless.::
import chatless.op2.UpdateInfo
import chatless.op2.InviteUser
import chatless.op2.RemoveTag
import chatless.op2.SetPublic
import chatless.op2.AddTag
import spray.routing.PathMatchers.PathEnd
import com.google.inject.Inject

trait TopicApi extends ServiceBase {

  private def canRead(cid: UserId, topic: TopicM): Boolean =
    (  topic.public
    || (topic.op == cid)
    || (topic.sops contains cid)
    || (topic.participating contains cid)
    )

  private val publicFields = (TopicM.TID :: TopicM.TITLE :: TopicM.PUBLIC :: Nil) map { _.name }
  private val participantFields =
    (TopicM.TID
    :: TopicM.TITLE
    :: TopicM.PUBLIC
    :: TopicM.INFO
    :: TopicM.OP
    :: TopicM.SOPS
    :: TopicM.PARTICIPATING
    :: TopicM.TAGS
    :: Nil) map { _.name }

  private def fieldsFor(cid: UserId, topic: TopicM): List[String] =
    if (canRead(cid, topic)) participantFields else publicFields

  private def getTopicInfo(cid: UserId)(topic: TopicM) = get {
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
  }

  private def completeUpdateTopic(cid: UserId, topic: TopicM)(op: UpdateSpec with ForTopics): Route =
    onSuccess(dbac.updateTopic(cid, topic.tid, op)) { completeBoolean }

  private def sopLevelUpdates(cup: UpdateSpec with ForTopics => Route): Route =
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

  private def opLevelUpdates(cup: UpdateSpec with ForTopics => Route): Route =
    put {
      path(TopicM.SOPS / Segment / PathEnd) { uid: UserId =>
        cup { PromoteSop(uid) }
      }
    } ~ delete {
      path(TopicM.SOPS / Segment / PathEnd) { uid: UserId =>
        cup { DemoteSop(uid) }
      }
    }

  private def update(cid: UserId, topic: TopicM): Route = {
    val cup = completeUpdateTopic(cid, topic) _
    authorize(topic.op == cid || (topic.sops contains cid)) {
      sopLevelUpdates(cup)
    } ~ authorize(topic.op == cid) {
      opLevelUpdates(cup)
    }
  }

  val topicApi: CallerRoute = cid => pathPrefix(TOPIC_API_BASE / Segment) { tid: TopicId =>
    onSuccess(dbac.getTopic(cid, tid)) { topic: TopicM =>
      getTopicInfo(cid)(topic) ~ update(cid, topic)
    }
  }
}
