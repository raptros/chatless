package chatless.responses

import chatless.{UserId, TopicId, MessageId}
import scalaz.NonEmptyList
import com.mongodb.casbah.Imports._

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

sealed abstract class StateError(msg: String) extends Throwable(msg) {
  def asJson: JValue = "msg" -> msg
}

case class UnhandleableMessageError(what: Any) extends StateError("can't handle ${what.getClass}.")

case class UserNotFoundError(uid: UserId)
  extends StateError(s"could not find user $uid")

case class ModelExtractionError(modelName: String, dbo: MongoDBObject, errors: NonEmptyList[String])
  extends StateError(s"problems deserializing $modelName model from $dbo: ${errors.list mkString "," }")

case class TopicNotFoundError(tid: TopicId)
  extends StateError(s"could not find topic $tid")


