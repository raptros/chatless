package chatless.model.js

import chatless._
import org.json4s._
import org.json4s.Extraction._
import chatless.model._


class ValueContainerSerializer extends CustomSerializer[ValueContainer](format => ({
    case js: JString => StringVC(js.s)
    case jb: JBool => BooleanVC(jb.value)
    case jo: JObject =>
      { extractOpt[Message](jo) map MessageVC.apply } orElse { extractOpt[Topic](jo) map TopicVC.apply } getOrElse FailedVC
  }, {
    case FailedVC => ???
    case StringVC(s) => JString(s)
    case BooleanVC(b) => JBool(b)
    case MessageVC(m) => decompose(m)
    case TopicVC(t) => decompose(t)
  })
)
