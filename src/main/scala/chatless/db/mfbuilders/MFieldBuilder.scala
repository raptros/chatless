package chatless.db.mfbuilders

import argonaut.CodecJson
import shapeless._
import Record._
import chatless.db._
import chatless.db.mfields.MField

abstract class MFieldBuilder[A1:CodecJson, M1, L <:HList](val l:L) {
  type ValUpdate = A1 => M1

  object fbName extends Field[String]
  object fbCanRead extends Field[CallerCan]
  object fbCanWrite extends Field[CallerCan]
  object fbResFor extends Field[CallerRes]
  object fbValue extends Field[A1]
  object fbUpdate extends Field[ValUpdate]

  def copy[L2 <: HList]:(L2) => MFieldBuilder[A1, M1, L2]

  val construct:(String, CallerCan, CallerCan, A1, CallerRes, ValUpdate) => MField[A1, M1]

  def newWith[V, F <: Field[V]](v:V, f:F)(implicit updater:Updater[L, F, V]):MFieldBuilder[A1, M1, Updater[L, F, V]#Out] =
    copy(l.updated(f, v)(updater))

  def withName(name:String) = newWith(name, fbName)

  def withCanRead(canRead:CallerCan) = newWith(canRead, fbCanRead)

  def withCanWrite(canWrite:CallerCan) = newWith(canWrite, fbCanWrite)

  def withValue(value:A1) = newWith(value, fbValue)

  def withResFor(resFor:CallerRes) = newWith(resFor, fbResFor)

  def withUpdate(update:ValUpdate) = newWith(update, fbUpdate)

  def build(implicit
    sel1:Selector[L, (fbName.type, fbName.valueType)],
    sel2:Selector[L, (fbCanRead.type, fbCanRead.valueType)],
    sel3:Selector[L, (fbCanWrite.type, fbCanWrite.valueType)],
    sel4:Selector[L, (fbValue.type, fbValue.valueType)],
    sel5:Selector[L, (fbResFor.type, fbResFor.valueType)],
    sel6:Selector[L, (fbUpdate.type, fbUpdate.valueType)]):MField[A1, M1] =
    construct(l get fbName, l get fbCanRead, l get fbCanWrite, l get fbValue, l get fbResFor, l get fbUpdate)
}

object MFieldBuilder {
  import scala.language.implicitConversions

  implicit def builder2MF[A, M, L <: HList](builder:MFieldBuilder[A, M, L])
    (implicit codec:CodecJson[A],
      sel1:Selector[L, (builder.fbName.type, builder.fbName.valueType)],
      sel2:Selector[L, (builder.fbCanRead.type, builder.fbCanRead.valueType)],
      sel3:Selector[L, (builder.fbCanWrite.type, builder.fbCanWrite.valueType)],
      sel4:Selector[L, (builder.fbValue.type, builder.fbValue.valueType)],
      sel5:Selector[L, (builder.fbResFor.type, builder.fbResFor.valueType)],
      sel6:Selector[L, (builder.fbUpdate.type, builder.fbUpdate.valueType)])
  :MField[A, M] = builder.build
}
