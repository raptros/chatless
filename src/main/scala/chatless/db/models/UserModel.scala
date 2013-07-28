package chatless.db.models

import chatless._
import scalaz.Validation
import argonaut._
import Argonaut._
import scalaz.Validation._
import scalaz.syntax.validation._
import scalaz.syntax.std.boolean._
import chatless.operation.{GetField, OpRes, ResMe, ResUser}
import chatless.db.mfbuilders.MFieldBuilders

case class UserModel(
    uid:UserId,
    nick:String,
    public:Boolean,
    info:Json,
    following:Set[UserId],
    followers:Set[UserId],
    blocked:Set[UserId],
    topics:Set[TopicId],
    tags:Set[String])
  extends AccessModel with MFieldBuilders {

  val resFor:(UserId => OpRes) = cid => if (cid == uid) ResMe else ResUser(cid)

  val fields = {
    readOnlyField[UserId] withName "uid" withValue uid withResFor resFor withCanRead {_ => true }
  } :: {
    replaceableField[String] withName "nick" withValue nick withResFor resFor withCanRead
      { _ => true } withCanWrite { cid => cid == uid } withUpdate { nv => copy(nick = nv) }
  } :: Nil

}

