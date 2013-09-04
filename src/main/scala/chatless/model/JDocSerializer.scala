package chatless.model

import org.json4s._

class JDocSerializer extends CustomSerializer[JDoc]( format => ({
    case jo: JObject => JDoc(jo.obj)
  }, {
    case a: JDoc => a
  })
)
