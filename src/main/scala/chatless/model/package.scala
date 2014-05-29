package chatless

package object model {
  import argonaut._
  import Argonaut._

  implicit class DecodeJsonOrElse[A](decodeA: DecodeJson[A]) {
    def orElse[B >: A](x: => DecodeJson[B]): DecodeJson[B] = DecodeJson { h =>
      decodeA.decode(h) ||| x.decode(h)
    }
  }

}
