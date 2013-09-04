package chatless.model

import org.json4s._

class JDoc(fs: List[JField]) extends JObject(fs)

object JDoc {
  def apply(fs: List[JField]) = new JDoc(fs)

  def apply(fs: JField*) = new JDoc(fs.toList)

  def unapply(jd: JDoc): Option[List[JField]] = Some(jd.obj)
}