
package object chatless {
  import spray.httpx.unmarshalling._
  import spray.http.HttpEntity
  import spray.http.MediaTypes._
  import scala.util.{Try, Success, Failure}
  import spray.httpx.marshalling._

  type UserId = String
  type MessageId = String
  type TopicId = String
  type EventId = String
  type RequestId = String

  implicit class TryToConv[A](attempt:Try[A]) {
    trait FailureConv[B] {
      val onSuc: A => B
      def convFailure(onFail: Throwable => B):B = attempt match {
        case Success(a) => onSuc(a)
        case Failure(t) => onFail(t)
      }
    }
    def convSuccess[B](onS: A => B):FailureConv[B] = new FailureConv[B] {
      val onSuc = onS
    }
  }

  import argonaut._
  import Argonaut._

  //  implicit def deserializeStringAsJson:Deserializer[String, Json] =
  import scalaz.\/._
  import scalaz.syntax.id._

  implicit def marshallJson:Marshaller[Json] = Marshaller.delegate(`application/json`) { json:Json => json.nospaces }

  implicit def marshallBoolean:Marshaller[Boolean] = Marshaller.delegate(`text/plain`) { bool:Boolean => bool.toString }

  implicit def jsonFromString:Deserializer[String, Json] = new Deserializer[String, Json] {
    val fail:String => Deserialized[Json] = e => MalformedContent(e).left[Json].toEither
    val success:Json => Deserialized[Json] = j => j.right[DeserializationError].toEither
    def apply(s:String):Deserialized[Json] = s.parseWith(success, fail)
  }

  implicit def fromStringUnmarshaller[A](implicit fs:Deserializer[String, A]):Unmarshaller[A] = new Deserializer[HttpEntity, A] {
    def apply(ent:HttpEntity):Deserialized[A] = BasicUnmarshallers.StringUnmarshaller(ent).right flatMap { dsd => fs(dsd) }
  }

//  implicit def jsonUnmarshaller:Unmarshaller(jdd)

}
