package chatless.services

import chatless._
import chatless.operation._

import shapeless._

import spray.routing._
import HListDeserializer._

import spray.httpx.unmarshalling.Deserializer._



trait MeApi extends ServiceBase with SpecDirectives {
  val me:Directive[UserId :: OpRes :: HNil] = (userAuth & pathPrefix("me")) map { cid:UserId =>
    cid :: ResMe.asInstanceOf[OpRes] :: HNil
  }
  private val gets:Directive1[OpSpec] = { path(PathEnd) & provide(GetAll) } |
    fieldPathGet("nick") |
    fieldPathGet("public") |
    fieldPathGet("info") |
    fieldPathGet("following") |
    listPathItemTest("following") |
    fieldPathGet("followers") |
    listPathItemTest("followers") |
    fieldPathGet("blocked") |
    listPathItemTest("blocked") |
    fieldPathGet("topics") |
    listPathItemTest("topics") |
    fieldPathGet("tags") |
    listPathItemTest("tags")

  private val puts:Directive1[OpSpec] = fieldPathReplace("nick") { StringVC.apply _ } |
    fieldPathReplace("public") { BooleanVC.apply _ } |
    fieldPathReplace("info") { JsonVC.apply _ } |
    listPathItemAppend("following") |
    listPathItemAppend("blocked") |
    listPathItemAppend("topics") |
    listPathItemAppend("tags")

  private val deletes:Directive1[OpSpec] = listPathItemDelete("following") |
    listPathItemDelete("followers") |
    listPathItemDelete("blocked") |
    listPathItemDelete("topics") |
    listPathItemDelete("tags")

  private val meReqs:Directive[UserId :: OpRes :: HNil] = (userAuth & pathPrefix("me" / "requests")) map { cid: UserId =>
    cid :: ResMeReqs.asInstanceOf[OpRes] :: HNil
  }

  private val getReq:Directive[UserId :: OpRes :: OpSpec :: HNil] = (get & userAuth &
    path("me" / "request" / Segment / PathEnd)) hmap {
    case cid :: rid :: HNil => cid :: ResRequest(rid).asInstanceOf[OpRes] :: GetAll.asInstanceOf[OpSpec] :: HNil
  }

  private val getting:Directive[UserId :: OpRes :: OpSpec :: HNil] = get & me & gets

  private val putting:Directive[UserId :: OpRes :: OpSpec :: HNil] = put & me & puts

  private val deleting:Directive[UserId :: OpRes :: OpSpec :: HNil] = delete & me & deletes

  private val reqsGets:Directive1[OpSpec] = { path(PathEnd) & provide(GetField("open")) } |
    fieldPathGet("accepted") |
    listPathItemTest("accepted") |
    fieldPathGet("rejected") |
    listPathItemTest("rejected")

  private val reqsPuts:Directive1[OpSpec] = listPathItemAppend("accepted") | listPathItemAppend("rejected")

  private val reqsGetting:Directive[UserId :: OpRes :: OpSpec :: HNil] = get & meReqs & reqsGets

  private val reqsPutting:Directive[UserId :: OpRes :: OpSpec :: HNil] = put & meReqs & reqsPuts

  val meApi:DOperation = (getting | putting | deleting | getReq | reqsGetting | reqsPutting) as { operation }
}
