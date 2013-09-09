package chatless.events.model

import org.json4s._

class ActionSerializer extends CustomSerializer[Action.Value](fmt => ({
  case JString(n) => Action.withName(n)
}, {
  case Action.Add => JString("add")
  case Action.Create => JString("create")
  case Action.Remove => JString("remove")
  case Action.Replace => JString("replace")
}))
