package chatless.model

import org.json4s._

class InfoSerializer extends CustomSerializer[Info]( format =>
  ({
    case jo: JObject => new Info(jo.values)
  }, {
    case a: Info =>
      val fields  = for { (k, v) <- a.m } yield JField(k, Extraction.decompose(v)(format))
      JObject(fields.toList)
  })
)
