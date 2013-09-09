import org.joda.time.DateTime

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

  implicit class TryToConv[A](attempt: Try[A]) {
    trait FailureConv[B] {
      val onSuc: A => B
      def convFailure(onFail: Throwable => B): B = attempt match {
        case Success(a) => onSuc(a)
        case Failure(t) => onFail(t)
      }
    }
    def convSuccess[B](onS: A => B): FailureConv[B] = new FailureConv[B] {
      val onSuc = onS
    }
  }

  implicit def marshallBoolean: Marshaller[Boolean] = Marshaller.delegate(`text/plain`) { bool: Boolean => bool.toString }


  type OptPair[+A, +B] = Option[(A, B)]

  import com.novus.salat.{ TypeHintFrequency, StringTypeHintStrategy, Context }

  import chatless.db.JDocStringTransformer

  implicit val ctx = new Context() {
    val name: String = "chatless"
    registerCustomTransformer(JDocStringTransformer)
  }

  import org.json4s._
  import chatless.model.JDocSerializer
  import chatless.events.model.ActionSerializer

  implicit val json4sFormats =
    DefaultFormats ++
      org.json4s.ext.JodaTimeSerializers.all +
      new JDocSerializer +
      new ActionSerializer
}
