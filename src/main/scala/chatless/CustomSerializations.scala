package chatless

import org.json4s.{CustomSerializer, Formats}
import org.json4s.JsonDSL._
import org.json4s.JsonAST._
import chatless.db._
import chatless.db.ResTopic
import chatless.db.ResUser
import chatless.db.ResTopic
import chatless.db.AppendToList
import chatless.db.DeleteFromList
import org.json4s.JsonAST.JString
import chatless.db.ResUser
import chatless.db.ReplaceField
import chatless.db.GetFields

object CustomSerializations {
  private def OpRes2JV(iFormats:Formats)(or:OpRes):JValue = or match {
    case ResUser(uid) => pair2Assoc("res" -> "user") ~ ("uid" -> uid)
    case ResTopic(tid) => pair2Assoc("res" -> "topic") ~ ("tid" -> tid)
  }

  private def pfOR2JV(iFormats:Formats):PartialFunction[Any, JValue] = {
    case or:OpRes => OpRes2JV(iFormats)(or)
  }

  private def jv2OR(iFormats:Formats):PartialFunction[JValue, OpRes] = {
    case JObject(JField("res", JString("user")) :: JField("uid", JString(uid)) :: Nil) => ResUser(uid)
    case JObject(JField("res", JString("topic")) :: JField("tid", JString(tid)) :: Nil) => ResTopic(tid)
  }

  class OpResSerializer extends CustomSerializer[OpRes]( formats => jv2OR(formats) -> pfOR2JV(formats) )

  private def OpSpec2JV(iFormats:Formats)(os:OpSpec):JValue = os match {
    case GetAll => { "op" -> "get" } ~ { "allfields" -> true }
    case GetFields(fields @ _* ) => {"op" -> "get"} ~ { "fields" -> fields }
    case ReplaceField(field, newVal) => { "op" -> "update" } ~ { "spec" -> "replace" } ~ { "value" -> newVal.toString }
    case AppendToList(field, newVal) => { "op" -> "update" } ~ {"spec" -> "append" } ~ { "value" -> newVal.toString }
    case DeleteFromList(field, oldVal) => { "op" -> "update" } ~ {"spec" -> "delete" } ~ { "oldVal" -> oldVal.toString }
  }

  private def pfOS2JV(iFormats:Formats):PartialFunction[Any, JValue] = {
    case os:OpSpec => OpSpec2JV(iFormats)(os)
  }

  private def jv2OS(iFormats:Formats):PartialFunction[JValue, OpSpec] = {
    case JObject(JField("op", JString("get")) :: JField("allfields", JBool(true)) :: Nil) => GetAll
    case JObject(JField("op", JString("get")) :: JField("fields", JArray(fields)) :: Nil) => GetFields(fields flatMap {
      case JString(s) => Some(s)
      case _ => None
    }: _*)
      //oh
    case JObject(JField("op", JString("update")) :: JField("spec", JString("replace")) :: JField("value", value) :: Nil) => GetAll
  }

  class OpResSerializer extends CustomSerializer[OpRes]( formats => jv2OR(formats) -> pfOR2JV(formats) )


  val customFormats = new OpResSerializer
}
