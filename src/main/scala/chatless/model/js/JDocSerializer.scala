package chatless.model.js

import org.json4s._
import chatless.model.JDoc

class JDocSerializer extends CustomSerializer[JDoc]( format => ({
    case jo: JObject => JDoc(jo.obj)
  }, {
    case a: JDoc => a
  })
)
