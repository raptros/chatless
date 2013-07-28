package chatless.db.mfbuilders

import reflect.runtime.universe._
import argonaut.CodecJson
import shapeless.HList
import chatless.db._
import chatless.db.mfields.ListMField

class ListMFB[A1:TypeTag:CodecJson, M1, L <: HList](l:L) extends MFieldBuilder[A1, M1, L](l) {
  def copy[L2 <: HList] = new ReplaceableMFB(_)

  val construct = new ListMField(_:String, _:CallerCan, _:CallerCan, _:A1, _:CallerRes, _:(A1 => M1))
}
