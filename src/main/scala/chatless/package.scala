import chatless.db.JDocStringTransformer
import org.joda.time.DateTime

package object chatless {
  import spray.httpx.unmarshalling._
  import spray.http.HttpEntity
  import spray.http.MediaTypes._
  import scala.util.{Try, Success, Failure}
  import spray.httpx.marshalling._

  type ServerId = String
  type UserId = String
  type TopicId = String
  type RequestId = String

  type EventId = String
  type MessageId = String


  import com.novus.salat.{ TypeHintFrequency, StringTypeHintStrategy, Context }


  implicit val ctx = new Context() {
    val name: String = "chatless"
    registerCustomTransformer(JDocStringTransformer)
    com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()
  }

}
