package object chatless {
  import org.json4s.DefaultFormats
  import org.json4s.native.Serialization
  import org.json4s.NoTypeHints

  import spray.httpx.unmarshalling._
  import spray.http.HttpEntity
  import scala.util.{Try, Success, Failure}

  type UserId = String
  type MessageId = String
  type TopicId = String
  type EventId = String

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

  import CustomSerializations._

  implicit val formats = Serialization.formats(NoTypeHints) + customFormats

  implicit def fromStringUnmarshaller[A](implicit um:Unmarshaller[String],
                                         fs:Deserializer[String, A]):Unmarshaller[A] = new Deserializer[HttpEntity, A] {
    def apply(ent:HttpEntity):Deserialized[A] = um(ent).right flatMap { dsd => fs(dsd) }
  }

}
