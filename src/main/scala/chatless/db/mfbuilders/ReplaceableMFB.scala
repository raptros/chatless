package chatless.db.mfbuilders

import argonaut.CodecJson
import shapeless.HList
import chatless.db.mfields.ReplaceableMField
import reflect.runtime.universe._

class ReplaceableMFB[A1:TypeTag:CodecJson, M1, L <: HList](l:L) extends MFieldBuilder[A1, M1, L](l) {
  def copy[L2 <: HList] = new ReplaceableMFB(_)

  val construct = ReplaceableMField.apply[A1, M1] _
}
