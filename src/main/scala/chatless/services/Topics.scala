package chatless.services

import spray.routing._
import HListDeserializer._
import spray.http._

import argonaut._
import Argonaut._
import argonaut.DecodeJson._
import argonaut.EncodeJson._

import shapeless._
import spray.httpx.unmarshalling.Deserializer._

import chatless.db._
import chatless._
import shapeless.::

import scala.reflect.runtime.universe._

trait Topics extends ServiceBase with SpecDirectives {

  private val topicsBase:Directive1[UserId] = userAuth & pathPrefix("topics")
  private val singleTopic:Directive[UserId :: TopicId :: HNil] = topicsBase & pathPrefix(Segment)
  //private val topicInfo:Directive[UserId :: TopicId :: HNil] = singleTopic & path("info" / PathEnd)

  val topicOperation:HListDeserializer[UserId :: TopicId :: OpSpec :: HNil, Operation] = {
    (cid:UserId, rtid:TopicId, spec:OpSpec) => Operation(cid, ResTopic(rtid), spec)
  }

  /** get a single topic handle */
  val getSingleTopic:DOperation = (get & singleTopic & path(PathEnd)) as {
    (cid:UserId, rtid:TopicId) => Operation(cid, ResTopic(rtid), GetAll)
  }

  val topicTitle:DOperation = (singleTopic & stringField("title")) as topicOperation

  val topicPublic:DOperation = (singleTopic & booleanField("public")) as topicOperation

  /** topic info */
  val topicInfo:DOperation = (singleTopic & jsonField("info")) as topicOperation

  /** all topics paths */
  val topicsApi:DOperation = getSingleTopic | topicTitle | topicPublic | topicInfo

}