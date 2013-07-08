package chatless.db

import chatless.{UserId, TopicId}

sealed abstract class OpRes
case class ResUser(uid:UserId) extends OpRes
case class ResTopic(tid:TopicId) extends OpRes

sealed abstract class OpSpec
sealed abstract class GetSpec extends OpSpec
case class GetFields(field:String*) extends GetSpec
case object GetAll extends GetSpec {
  def apply(a:Any) = this
}

sealed abstract class UpdateSpec[+A] extends OpSpec
case class ReplaceField[A](field:String, value:A) extends UpdateSpec
case class AppendToList[A](field:String, value:A) extends UpdateSpec
case class DeleteFromList[A](field:String, value:A) extends UpdateSpec

case class Operation(cid:UserId, res:OpRes, opSpec:OpSpec)







