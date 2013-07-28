package chatless.db.mfields

import argonaut._
import chatless.db._
import reflect.runtime.universe._
import chatless.operation._
import scalaz._
import scalaz.syntax.validation._
import chatless.db.OperationNotSupported
import chatless.operation.AppendToList
import chatless.operation.ReplaceField
import chatless.db.AccessNotPermitted

class ListMField[A:TypeTag:CodecJson, M](name:String, canRead:CallerCan, canWrite:CallerCan, value:A, resFor:CallerRes, update:(A => M))
  extends MField[A, M](name, canRead, canWrite, value, resFor, update) {

  def getFor(cid:chatless.UserId, spec:GetSpec):scalaz.ValidationNel[StateError, A] =
    if (canRead(cid)) value.successNel[StateError] else AccessNotPermitted(cid, resFor(cid), spec).failNel

  def updateFor(cid:chatless.UserId, spec:UpdateSpec):scalaz.ValidationNel[StateError, M] = spec match {
    case _:ReplaceField => OperationNotSupported(cid, resFor(cid), spec).failNel
    case _:AppendToList => OperationNotSupported(cid, resFor(cid), spec).failNel //todo
    case _:DeleteFromList => OperationNotSupported(cid, resFor(cid), spec).failNel //todo
  }
}

object ListMField {
  def apply[A:TypeTag:CodecJson, M](
    name:String,
    canRead:CallerCan,
    canWrite:CallerCan,
    value:A,
    resFor:CallerRes,
    update:(A => M)) = new ListMField[A, M](name, canRead, canWrite, value, resFor, update)
}
