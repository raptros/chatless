package chatless.db.mfields

import argonaut.CodecJson
import scala.reflect.runtime.universe._

import scalaz._
import scalaz.syntax.validation._

import chatless._
import chatless.operation._
import chatless.db._

class ReplaceableMField[A:TypeTag:CodecJson, M](name:String, canRead:CallerCan, canWrite:CallerCan, value:A, resFor:CallerRes, update:(A => M))
  extends MField[A, M](name, canRead, canWrite, value, resFor, update) {

  def getFor(cid:UserId, spec:GetSpec):ValidationNel[StateError, A] =
    if (canRead(cid)) value.successNel[StateError] else AccessNotPermitted(cid, resFor(cid), spec).failNel

  def updateFor(cid:UserId, spec:UpdateSpec):ValidationNel[StateError, M] = spec match {
    case ReplaceField(field, value) => if (canWrite(cid)) doReplace(cid, value) else
      AccessNotPermitted(cid, resFor(cid), spec).failNel
    case _:AppendToList => OperationNotSupported(cid, resFor(cid), spec).failNel
    case _:DeleteFromList => OperationNotSupported(cid, resFor(cid), spec).failNel
  }

  protected def doReplace(cid:UserId, vc:ValueContainer):ValidationNel[StateError, M] = if (vc.tpe =:= typeOf[A])
    update(vc.contained.asInstanceOf[A]).successNel
  else OperationNotSupported(cid, resFor(cid), ReplaceField(name, vc)).failNel
}

object ReplaceableMField {
  def apply[A:TypeTag:CodecJson, M](
    name:String,
    canRead:CallerCan,
    canWrite:CallerCan,
    value:A,
    resFor:CallerRes,
    update:(A => M)) = new ReplaceableMField[A, M](name, canRead, canWrite, value, resFor, update)
}
