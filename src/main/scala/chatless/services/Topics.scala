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

trait Topics extends ServiceBase {

  private val topicsBase:Directive1[UserId] = userAuth & pathPrefix("topics")
  private val singleTopic:Directive[UserId :: TopicId :: HNil] = topicsBase & pathPrefix(Segment)
  private val topicTitle:Directive[UserId :: TopicId :: HNil] = singleTopic & path("title" / PathEnd)
  private val topicPublic:Directive[UserId :: TopicId :: HNil] = singleTopic & path("public" / PathEnd)
  private val topicInfo:Directive[UserId :: TopicId :: HNil] = singleTopic & path("info" / PathEnd)

  /** get the topic handles of the topics the user is participating in*/
  val getTopics:DOperation = (get & topicsBase & path(PathEnd)) as { cid:UserId =>
    Operation(cid, ResUser(cid), GetFields("topics"))
  }

  /** get a single topic handle */
  val getSingleTopic:DOperation = (get & singleTopic & path(PathEnd)) as { (cid:UserId, rtid:TopicId) =>
    Operation(cid, ResTopic(rtid), GetAll)
  }

  /** get a topic title*/
  val getTopicTitle:DOperation = (get & topicTitle) as { (cid:UserId, rtid:TopicId) =>
    Operation(cid, ResTopic(rtid), GetFields("title"))
  }

  /** set the topic title */
  val putTopicTitle:DOperation = (put & topicTitle & dEntity(as[String])) as { (cid:UserId, rtid:TopicId, nTitle:String) =>
    Operation(cid, ResTopic(rtid), ReplaceField("title", nTitle))
  }

  /** is topic public ? */
  val getTopicPublic:DOperation = (get & topicPublic) as { (cid:UserId, rtid:TopicId) =>
    Operation(cid, ResTopic(rtid), GetFields("public"))
  }

  /** modify the publicness */
  val putTopicPublic:DOperation = (put & topicPublic & dEntity(as[Boolean])) as { (cid:UserId, rtid:TopicId, nPub:Boolean) =>
    Operation(cid, ResTopic(rtid), ReplaceField("public", nPub))
  }

  /** get topic info */
  val getTopicInfo:DOperation = (get & topicInfo) as { (cid:UserId, rtid:TopicId) =>
    Operation(cid, ResTopic(rtid), GetFields("info"))
  }

  /** modify the info */
  val putTopicInfo:DOperation = (put & topicInfo & dEntity(as[String])) as { (cid:UserId, rtid:TopicId, nInfo:String) =>
    Operation(cid, ResTopic(rtid), ReplaceField("info", nInfo))
  }

  /** all topics paths */
  val topicsApi:DOperation = getTopics |
    getSingleTopic |
    getTopicTitle |
    putTopicTitle |
    getTopicPublic |
    putTopicPublic |
    getTopicInfo |
    putTopicInfo

}