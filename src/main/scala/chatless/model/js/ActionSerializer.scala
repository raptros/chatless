package chatless.model.js

import org.json4s._
import chatless.model.Action

class ActionSerializer extends CustomSerializer[Action.Value](format => ({
    case js: JString => Action.withName(js.s)
  }, {
    case a: Action.Value => JString(a.toString)
  })
)
