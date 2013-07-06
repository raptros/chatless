
/**
 * Created with IntelliJ IDEA.
 * User: aidan
 * Date: 7/5/13
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
package object chatless {
  import org.json4s.native.Serialization
  import org.json4s.NoTypeHints
  import org.json4s._
  import org.json4s.JsonAST.JValue
  import org.json4s.JsonDSL._

  import scala.util.{Try, Success, Failure}

  implicit val formats = Serialization.formats(NoTypeHints)


  type UserId = String
  type MessageId = String
  type TopicId = String

  type EventId = String

  case class RequestError(resourceRef:String, explanation:String)

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

  def throwableToJson(t:Throwable):JObject = "failed" -> {
    pair2Assoc("exception" -> JString(t.getClass.toString)) ~
      ("message" -> Option(t.getMessage))
  }

  def routeEnvToJson(cid:Option[UserId]=None,
                     rtid:Option[TopicId]=None):JObject = "env" -> {
    pair2Assoc("cid" -> cid) ~ ("rtid" -> rtid)
  }

}
