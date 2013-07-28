package chatless.db.mfbuilders

import argonaut._
import shapeless._
import scala.reflect.runtime.universe._

//yep, these things are needed here.
import scala.language.existentials

trait MFieldBuilders { self =>
  def readOnlyField[A:TypeTag:CodecJson] =
    new ReadOnlyMFB[A, self.type, HNil](HNil) withCanWrite { _ => false } withUpdate { _ => self }

  def replaceableField[A:TypeTag:CodecJson] =
    new ReplaceableMFB[A, self.type, HNil](HNil)


}

