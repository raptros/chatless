package chatless.db


import org.json4s._
import com.novus.salat.transformers.CustomTransformer
import org.json4s.native.JsonMethods._
import chatless.model.JDoc

object JDocStringTransformer extends CustomTransformer[JDoc, String] {
  def deserialize(s: String): JDoc = JDoc(parse(s).asInstanceOf[JObject].obj)

  def serialize(j: JDoc): String = compact(render(j))
}

