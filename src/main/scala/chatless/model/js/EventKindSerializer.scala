package chatless.model.js

import org.json4s._
import chatless.model.EventKind

class EventKindSerializer extends CustomSerializer[EventKind.Value](format => ({
    case js: JString => EventKind.withName(js.s)
  }, {
    case a: EventKind.Value => JString(a.toString)
  })
)
