package chatless.db.mongo

import chatless.model.topic.Topic

package object parsers {

  import com.osinka.subset._
  import chatless.model._

  import scalaz.\/
  import chatless.db.DeserializationErrors

  import argonaut._
  import Argonaut._
  import com.mongodb.casbah.Imports._

  import com.mongodb.casbah.commons.conversions.scala._
  RegisterJodaTimeConversionHelpers()

  implicit val messageParser: DocParser[Message] = Parsers.messageParser

  implicit val topicParser: DocParser[Topic] = Parsers.topicParser

  import scalaz.syntax.std.either._
  import DocParser._

  implicit class ParseDBO(dbo: DBObject) {
    def parseAs[A](implicit p: DocParser[A]): DeserializationErrors \/ A =  p(dbo).disjunction leftMap { err =>
      DeserializationErrors(List(err))
    }

    def getField[A: Field](f: Fields.Field): DeserializationErrors \/ A = 
      get[A](f.toString).apply(dbo).disjunction leftMap { err =>
        DeserializationErrors(List(err))
      }
  }
}
