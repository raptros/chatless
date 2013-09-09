package chatless.events.model

import chatless._
import org.json4s._
import scalaz.syntax.id._
import scalaz.syntax.apply._
import scalaz.std.option._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import chatless.model.{JDoc, User}

object UserDeltas extends Deltas {
  val resource = Resource.User

  trait ReplaceField extends Matcher2 {
    val action = Action.Replace
    type A1 = UserId
    val field: String
    def extractNewVal(jv: JValue): Option[A2]
    def render(a2: A2): JValue
    def matchField(f: Option[String]) = f some { _ == field } none false
    def extract(d: Delta) = ^(d.cid, d.newVal flatMap { extractNewVal _ }) { _ -> _ }
    def set(c: UserId, a2: A2)(d: Delta) = d.copy(cid = c.some, newVal = render(a2).some)

  }

  object SetNick extends ReplaceField {
    type A2 = String
    val field = User.NICK
    def extractNewVal(jv: JValue) = jv.extractOpt[String]
    def render(nick: String) = JString(nick)
  }

  object SetPublic extends ReplaceField {
    type A2 = Boolean
    val field = User.PUBLIC
    def extractNewVal(jv: JValue) = jv.extractOpt[Boolean]
    def render(public: Boolean) = JBool(public)
  }

  object SetInfo extends ReplaceField {
    type A2 = JDoc
    val field = User.INFO
    def extractNewVal(jv: JValue) = jv match {
      case JObject(obj) => JDoc(obj).some
      case _ => None
    }
    def render(i: JDoc) = i
  }


}
