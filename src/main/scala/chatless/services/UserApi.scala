package chatless.services

import spray.routing._
import HListDeserializer._


import shapeless._
import spray.httpx.unmarshalling.Deserializer._

import chatless.db._
import chatless._
import shapeless.::

trait UserApi extends ServiceBase with SpecDirectives {
  val user:Directive[UserId :: OpRes :: HNil] = (userAuth & pathPrefix("user" / Segment)) hmap {
    case cid :: uid :: HNil => cid :: ResUser(uid).asInstanceOf[OpRes] :: HNil
  }

  private val gets:Directive1[OpSpec] = { path(PathEnd) & provide(GetAll) } |
    fieldPathGet("nick") |
    fieldPathGet("public") |
    fieldPathGet("info") |
    fieldPathGet("following") |
    listPathItemTest("following") |
    fieldPathGet("followers") |
    listPathItemTest("followers") |
    fieldPathGet("topics") |
    listPathItemTest("topics")

  private val getting:Directive[UserId :: OpRes :: OpSpec :: HNil] = get & user & gets

  private val userReqs:Directive[UserId :: OpRes :: HNil] = user hmap {
    case cid :: ResUser(uid) :: HNil => cid :: ResUserReqs(uid).asInstanceOf[OpRes] :: HNil
  }

  private val getReq:Directive[UserId :: OpRes :: OpSpec :: HNil] = (get & user & path("request" / Segment / PathEnd)) hmap {
    case cid :: ResUser(uid) :: rid :: HNil => cid :: ResRequest(rid).asInstanceOf[OpRes] :: GetAll.asInstanceOf[OpSpec] :: HNil
  }

  private val reqsPosting:Directive[UserId :: OpRes :: OpSpec :: HNil] = userReqs & createJson

  val userApi:DOperation = (getting | getReq | reqsPosting) as { operation }

}
