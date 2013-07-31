package chatless.op2

import argonaut.CodecJson

sealed abstract class UpdateSpec[A:CodecJson] {
  val fieldName:String
  val target:A
}

case class ReplaceField[A:CodecJson](fieldName:String, target:A) extends UpdateSpec[A]

case class AddToSet[A:CodecJson](fieldName:String, target:A) extends UpdateSpec[A]

case class RemoveFromSet[A:CodecJson](fieldName:String, target:A) extends UpdateSpec[A]
