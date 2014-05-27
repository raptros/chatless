package chatless.db.mongo.codecs

import argonaut._
import Argonaut._

import io.github.raptros.bson._
import Bson._

import scalaz.NonEmptyList

trait JsonCodec {
  implicit val jsonEncodeBsonField: EncodeBsonField[Json] = stringEncodeField contramap { j =>
    j.nospaces
  }

  implicit val jsonDecodeBsonField: DecodeBsonField[Json] = DecodeBsonField { (k, dbo) =>
    for {
      s <- stringDecodeField(k, dbo)
      json <- s.parse leftMap { msg => NonEmptyList(CustomError(msg)) }
    } yield json
  }


}
