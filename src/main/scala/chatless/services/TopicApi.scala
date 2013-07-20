package chatless.services

import spray.routing._
import HListDeserializer._


import shapeless._
import spray.httpx.unmarshalling.Deserializer._

import chatless.db._
import chatless._
import shapeless.::
import chatless.operation._
import chatless.operation.BooleanVC
import shapeless.::
import chatless.operation.JsonVC
import chatless.operation.StringVC


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

  val reqsApi = {
    val topicReqs:Directive[UserId :: OpRes :: HNil] = (topic & pathPrefix("requests"))  hmap {
      case cid :: ResTopic(tid) :: HNil => cid :: ResTopicReqs(tid).asInstanceOf[OpRes] :: HNil
    }

    val getReq:Directive[UserId :: OpRes :: OpSpec :: HNil] = (get & topic & path("request" / Segment / PathEnd)) hmap {
      case cid :: ResTopic(tid) :: rid :: HNil => cid :: ResRequest(rid).asInstanceOf[OpRes] :: GetAll.asInstanceOf[OpSpec] :: HNil
    }

    val reqsGets:Directive1[OpSpec] = { path(PathEnd) & provide(GetFields("open")) } |
      fieldPathGet("accepted") |
      listPathItemTest("accepted") |
      fieldPathGet("rejected") |
      listPathItemTest("rejected")

    val reqsPuts:Directive1[OpSpec] = listPathItemAppend("accepted") | listPathItemAppend("rejected")
    val reqsGetting:Directive[UserId :: OpRes :: OpSpec :: HNil] = get & topicReqs & reqsGets
    val reqsPutting:Directive[UserId :: OpRes :: OpSpec :: HNil] = put & topicReqs & reqsPuts
    val reqsPosting:Directive[UserId :: OpRes :: OpSpec :: HNil] = topicReqs & createJson

    getReq | reqsGetting | reqsPutting | reqsPosting
  }

  val messages:Directive[UserId :: OpRes :: HNil] = (topic & pathPrefix("message")) hmap {
    case cid :: ResTopic(tid) :: HNil => cid :: ResMessages(tid).asInstanceOf[OpRes] :: HNil
  }

  val getMessage:Directive1[OpSpec] = Relative.default(true) |
    Relative.first |
    Relative.last |
    Relative.at |
    Relative.before |
    Relative.from |
    Relative.after

  val messageApi:Directive[UserId :: OpRes :: OpSpec :: HNil] = (get & messages & getMessage) | (post & messages & path(PathEnd) & createJson)

  val topicApi:DOperation = (getting | putting | deleting | reqsApi | messageApi) as { operation }
}
