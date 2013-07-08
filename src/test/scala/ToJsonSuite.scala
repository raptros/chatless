import com.mongodb.casbah.util.UpdateOp
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.scalatest._

import chatless.ToJson

import chatless.db._

class ToJsonSuite extends FunSuite {

  test("an explicit update spec becomes the right sort of json") {
    val upSpec:UpdateSpec[Int] = ReplaceField("pants", 355)
    val js = ToJson(upSpec)
    assert(js \ "op" === JString("update"))
    assert(js \ "spec" === JString("replace"))
    assert(js \ "value" === JString("355"))
  }


}
