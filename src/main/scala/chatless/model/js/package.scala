package chatless.model

package object js {
  import org.json4s._
  lazy val formats = DefaultFormats + new EventKindSerializer + new JDocSerializer + new ActionSerializer + new ValueContainerSerializer
}
