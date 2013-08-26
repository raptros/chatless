package chatless.db

import chatless.{UserId, TopicId, MessageId}
import argonaut._
import Argonaut._
import chatless.op2.{Operation, Specifier}
import scalaz.NonEmptyList
import com.mongodb.casbah.Imports._

sealed abstract class StateError(msg: String) extends Throwable(msg) {
  def asJson: Json = ("msg" := msg) ->: jEmptyObject
}

case class UnhandleableMessageError(what: Any) extends StateError("can't handle ${what.getClass}.")

case class UserNotFoundError(uid: UserId)
  extends StateError(s"could not find user $uid")

case class ModelExtractionError(modelName: String, dbo: MongoDBObject, errors: NonEmptyList[String])
  extends StateError(s"problems deserializing $modelName model from $dbo: ${errors.list mkString "," }")

case class TopicNotFoundError(tid: TopicId)
  extends StateError(s"could not find topic $tid")

case class OperationNotSupportedError(op: Operation)
  extends StateError(s"cannot perform operation $op")


/*
case class NonExistentField(field: String, res: Resource)
  extends StateError(s"cannot get field $field for $res")
*/

/*
case class AccessNotPermitted(cid: UserId, res: Resource, spec: Specifier)
  extends StateError(s"caller $cid is not allowed to do $spec on resource $res")
*/

