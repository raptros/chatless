package chatless

/*
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import scala.reflect.runtime.universe._
import chatless.db._
*/
/*
object ToJson {
  def apply(h:Handle):JValue = h match {
    case TopicHandle(tid, opid, title) => pair2Assoc("tid" -> tid) ~ ("opid" -> opid) ~ ("title" -> title)
    case UserHandle(uid, nick, public) => pair2Assoc("uid" -> uid) ~ ("nick" -> nick) ~ ("public" -> public)
  }

  def apply(res:OpRes):JValue = res match {
    case ResUser(uid) => pair2Assoc("res" -> "user") ~ ("uid" -> uid)
    case ResTopic(tid) => pair2Assoc("res" -> "topic") ~ ("tid" -> tid)
  }


  def apply(spec:OpSpec):JValue = spec match {
    case (op:GetSpec) => {"op" -> "get"}
    case ReplaceField(field, newVal) => pair2Assoc("op" -> "update") ~ ("spec" -> "replace") ~ ("value" -> newVal.toString)
    case AppendToList(field, newVal) => pair2Assoc("op" -> "update") ~ ("spec" -> "append") ~ ("value" -> newVal.toString)
    case DeleteFromList(field, oldVal) => pair2Assoc("op" -> "update") ~ ("spec" -> "delete") ~ ("oldVal" -> oldVal.toString)
  }

  def apply(se:StateError):JValue = se match {
    case TopicNotFoundError(tid, cid) => pair2Assoc("tid" -> tid) ~ ("cid" -> cid)
    case OperationNotSupported(cid, res, spec) => pair2Assoc("cid" -> cid) ~ ("res" -> apply(res)) ~ ("spec" -> apply(spec))
  }

  def apply(t:Throwable):JValue = t match {
    case (se:StateError) => apply(se)
    case _ => "failed" -> {
      pair2Assoc("exception" -> JString(t.getClass.toString)) ~
        ("message" -> Option(t.getMessage))
    }
  }
}*/
