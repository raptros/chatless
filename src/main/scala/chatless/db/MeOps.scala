package chatless.db
import chatless._
import chatless.operation._

import argonaut._
import Argonaut._

import com.mongodb.casbah.Imports._

trait MeOps { this:DbActor =>
  import UserObjects.UserDBOOps

  def loadField(uid:UserId, field:String):Option[Json] = users.findOne("uid" $eq uid, Map(field -> 1)) flatMap  { dbo =>
    wrapDBObj(dbo) getJson field
  }

  def getForMe(cid:UserId, spec:GetSpec):Json = spec match {
    case GetAll => users.findOne("uid" $eq cid, UserObjects.fieldProj) map { dbo => UserObjects.getUserJson(dbo) } getOrElse jEmptyObject
    case GetField(field) => (field :=? loadField(cid, field)) ->?: jEmptyObject
    case GetListContains(field, value) => ("res" := users.findOne($and("uid" $eq cid, field $eq value)).nonEmpty) ->: jEmptyObject
    case relative:GetRelative => throw OperationNotSupported(cid, ResMe, relative)
  }

  def updateMe(cid:UserId, spec:UpdateSpec) = spec match {
    case ReplaceField(field, value) => users.update("uid" $eq cid, vc2DBO(field, value))
    case AppendToList(field, value) => users.update("uid" $eq cid, vc2DBO(field, value))
    case DeleteFromList(field, value) => users.update("uid" $eq cid, vc2DBO(field, value))
  }

  def handleMe(cid:UserId, spec:OpSpec):Json = spec match {
    case get:GetSpec => getForMe(cid, get)
    case update:UpdateSpec => updateMe(cid, update); jEmptyObject
    case create:Create => throw OperationNotSupported(cid, ResMe, create)
  }
}
