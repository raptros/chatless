package chatless.db.mfields

import argonaut.CodecJson
import scala.reflect.runtime.universe._

import chatless._
import chatless.db._
import chatless.operation.{UpdateSpec, GetSpec}

import scalaz._
import scalaz.syntax.validation._

class ReadOnlyMField[A:TypeTag:CodecJson, M](name:String, canRead:CallerCan, canWrite:CallerCan, value:A, resFor:CallerRes, update:(A => M))
  extends MField[A, M](name, canRead, canWrite, value, resFor, update) {

  def getFor(cid:UserId, spec:GetSpec):ValidationNel[StateError, A] =
    if (canRead(cid)) value.successNel[StateError] else AccessNotPermitted(cid, resFor(cid), spec).failNel

  def updateFor(cid:UserId, spec:UpdateSpec):ValidationNel[StateError, M] =
    OperationNotSupported(cid, resFor(cid), spec).failNel
}

object ReadOnlyMField {
  def apply[A:TypeTag:CodecJson, M](
    name:String,
    canRead:CallerCan,
    canWrite:CallerCan,
    value:A,
    resFor:CallerRes,
    update:(A => M)) = new ReadOnlyMField[A, M](name, canRead, canWrite, value, resFor, update)
}