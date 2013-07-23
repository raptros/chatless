package chatless.db
import chatless._
import shapeless._
import argonaut._
import Argonaut._

import com.mongodb.casbah.Imports._

object UserObjects extends DBObjects {

  object uid extends BaseDBOFE[String]("uid")
  object nick extends BaseDBOFE[String]("nick")
  object public extends BaseDBOFE[Boolean]("public")
  object info extends BaseDBOFE[String]("info")
  object following extends BaseDBOFE[List[UserId]]("following")
  object followers extends BaseDBOFE[List[UserId]]("followers")
  object blocked extends BaseDBOFE[List[UserId]]("blocked")
  object topics extends BaseDBOFE[List[TopicId]]("topics")
  object tags extends BaseDBOFE[List[String]]("tags")

  val fields:List[BaseDBOFE[_]] = List(uid, nick, public, info, following, followers, blocked, topics, tags)

  implicit class UserDBOOps(val dbo:MongoDBObject) extends DBObjectOps

  def getUserJson(dbo:MongoDBObject):Json = {
    val pairs = fmap.keys flatMap { k => k :=? (dbo getJson k) }
    (pairs foldRight jEmptyObject) { _ ->: _ }
  }
}
