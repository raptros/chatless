import chatless.model.js.{JDocSerializer}
import org.joda.time.DateTime

package object chatless {
  import spray.httpx.unmarshalling._
  import spray.http.HttpEntity
  import spray.http.MediaTypes._
  import scala.util.{Try, Success, Failure}
  import spray.httpx.marshalling._

  type UserId = String
  type TopicId = String
  type RequestId = String

  type EventId = String
  type MessageId = String

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


  type OptPair[+A, +B] = Option[(A, B)]

  import com.novus.salat.{ TypeHintFrequency, StringTypeHintStrategy, Context }

  import chatless.db.JDocStringTransformer

  implicit val ctx = new Context() {
    val name: String = "chatless"
    registerCustomTransformer(JDocStringTransformer)
    com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()
  }

  implicit val json4sFormats = chatless.model.js.formats

}
