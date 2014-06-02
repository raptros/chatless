package chatless.db.mongo.codecs

import io.github.raptros.bson._
import Bson._
import chatless.model._
import chatless.model.ids._
import scalaz._
import com.mongodb.DBObject

trait IdCodecs {
//  implicit def ServerIdDecodeBsonField: DecodeBsonField[String @@ ServerId] =
//    stringDecodeField map { ServerId.apply }
//
//  implicit def UserIdDecodeBsonField: DecodeBsonField[String @@ UserId] =
//    stringDecodeField map { UserId }
//
//  implicit def TopicIdDecodeBsonField: DecodeBsonField[String @@ TopicId] =
//    stringDecodeField map { TopicId }
//
//  implicit def MessageIdDecodeBsonField: DecodeBsonField[String @@ MessageId] =
//    stringDecodeField map { MessageId }

  implicit def TaggedStringDecodeBsonField[T]: DecodeBsonField[String @@ T] =
    stringDecodeField map { Tag.of[T].apply }

//  implicit def ServerIdEncodeBsonField: EncodeBsonField[String @@ ServerId] =
//    stringEncodeField.contramap[String @@ ServerId] { identity }

  implicit def TaggedStringEncodeBsonField[T]: EncodeBsonField[String @@ T] =
    stringEncodeField.contramap[String @@ T] { identity}

}
