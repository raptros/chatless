package chatless.model

import java.text.SimpleDateFormat

package object js {
  import org.json4s._
  lazy val defaultFormats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  }

  lazy val formats = defaultFormats +
    //wow, such ease, so working, amaze
    new org.json4s.ext.EnumNameSerializer(EventKind) +
    new org.json4s.ext.EnumNameSerializer(Action) +
    new JDocSerializer +
    new ValueContainerSerializer ++
    org.json4s.ext.JodaTimeSerializers.all
}
