package chatless.db.mfbuilders

import argonaut.CodecJson
import shapeless.HList
import chatless.db.mfbuilders.MFieldBuilder
import chatless.db.mfields.ReadOnlyMField
import reflect.runtime.universe._

class ReadOnlyMFB[A1:TypeTag:CodecJson, M1, L <: HList](l:L) extends MFieldBuilder[A1, M1, L](l) {
  def copy[L2 <: HList] = new ReadOnlyMFB(_)

  val construct = ReadOnlyMField.apply[A1, M1] _
}
