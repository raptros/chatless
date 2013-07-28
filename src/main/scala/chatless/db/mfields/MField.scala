package chatless.db.mfields

import argonaut.CodecJson
import chatless.db._
import chatless._
import chatless.operation.{UpdateSpec, GetSpec}
import scalaz._
import scala.reflect.runtime.universe._

abstract class MField[A:TypeTag:CodecJson, M](
  val name:String,
  val canRead:CallerCan,
  val canWrite:CallerCan,
  val value:A,
  val resFor:CallerRes,
  val update:(A => M)) {

  def getFor(cid:UserId, spec:GetSpec):ValidationNel[StateError, A]

  def updateFor(cid:UserId, spec:UpdateSpec):ValidationNel[StateError, M]
}



