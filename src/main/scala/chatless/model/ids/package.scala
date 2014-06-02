package chatless.model

package object ids {
  import scalaz._
  import argonaut._
  import Argonaut._

  sealed trait RootId

  sealed trait ServerId

  sealed trait UserId

  sealed trait TopicId

  sealed trait MessageId

  val RootId = Tag.of[RootId]

  val ServerId = Tag.of[ServerId]

  val UserId = Tag.of[UserId]

  val TopicId = Tag.of[TopicId]

  val MessageId = Tag.of[MessageId]

  implicit class StringTagger(s: String) {
    def serverId = ServerId(s)
    def userId = UserId(s)
    def topicId = TopicId(s)
    def messageId = MessageId(s)
  }

  @inline implicit def taggedDecodeJson[T]: DecodeJson[String @@ T] = StringDecodeJson map { s =>
    Tag.of[T](s)
  }


}
