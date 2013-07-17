package chatless.services

import spray.routing._
import HListDeserializer._


import shapeless._
import spray.httpx.unmarshalling.Deserializer._

import chatless.db._
import chatless._
import shapeless.::


trait TopicApi extends ServiceBase with SpecDirectives {

  val topic:Directive[UserId :: OpRes :: HNil] = (userAuth & pathPrefix("topic" / Segment)) hmap {
    case cid :: tid :: HNil => cid :: ResTopic(tid).asInstanceOf[OpRes] :: HNil
  }

  private val gets:Directive1[OpSpec] = { path(PathEnd) & provide(GetAll) } |
    fieldPathGet("title") |
    fieldPathGet("public") |
    fieldPathGet("info") |
    fieldPathGet("tags") |
    listPathItemTest("tags") |
    fieldPathGet("op") |
    fieldPathGet("sops") |
    listPathItemTest("sops") |
    fieldPathGet("participating") |
    listPathItemTest("participating")

  private val puts:Directive1[OpSpec] = fieldPathReplace("title") { StringVC.apply _ } |
    fieldPathReplace("public") { BooleanVC.apply _ } |
    fieldPathReplace("info") { JsonVC.apply _ } |
    listPathItemAppend("tags") |
    listPathItemAppend("sops") |
    listPathItemAppend("participating")

  private val deletes:Directive1[OpSpec] = listPathItemDelete("tags") |
    listPathItemDelete("sops") |
    listPathItemDelete("participating")

  private val getting:Directive[UserId :: OpRes :: OpSpec :: HNil] = get & topic & gets

  private val putting:Directive[UserId :: OpRes :: OpSpec :: HNil] = put & topic & puts

  private val deleting:Directive[UserId :: OpRes :: OpSpec :: HNil] = delete & topic & deletes

  private val topicReqs:Directive[UserId :: OpRes :: HNil] = (topic & pathPrefix("requests"))  hmap {
    case cid :: ResTopic(tid) :: HNil => cid :: ResTopicReqs(tid).asInstanceOf[OpRes] :: HNil
  }

  private val getReq:Directive[UserId :: OpRes :: OpSpec :: HNil] = (get & topic & path("request" / Segment / PathEnd)) hmap {
    case cid :: ResTopic(tid) :: rid :: HNil => cid :: ResRequest(rid).asInstanceOf[OpRes] :: GetAll.asInstanceOf[OpSpec] :: HNil
  }

  private val reqsGets:Directive1[OpSpec] = { path(PathEnd) & provide(GetFields("open")) } |
    fieldPathGet("accepted") |
    listPathItemTest("accepted") |
    fieldPathGet("rejected") |
    listPathItemTest("rejected")

  private val reqsPuts:Directive1[OpSpec] = listPathItemAppend("accepted") | listPathItemAppend("rejected")

  private val reqsGetting:Directive[UserId :: OpRes :: OpSpec :: HNil] = get & topicReqs & reqsGets

  private val reqsPutting:Directive[UserId :: OpRes :: OpSpec :: HNil] = put & topicReqs & reqsPuts

  private val reqsPosting:Directive[UserId :: OpRes :: OpSpec :: HNil] = topicReqs & createJson

  val topicApi:DOperation = (getting | putting | deleting | getReq | reqsGetting | reqsPutting | reqsPosting) as { operation }

}