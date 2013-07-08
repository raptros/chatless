import chatless.Topics
import chatless.db._
import org.scalatest.{FunSpec, WordSpec}


class TopicsRouteSpec extends FunSpec with ServiceSpecBase with Topics
{
  val apiInspector = topicsApi { dbReq => complete { dbReq } }

  describe("the operation object returned when the topics api receives a") {
    describeResultOf(Get("/topics/")) { op =>
      it("contains the user's id") {
        op should have (cid (userId))
      }
      it("selects that user's resource") {
        op should have (res (ResUser(userId)))
      }
      it("specifies the topics field") {
        op should have (opSpec (GetFields("topics")))
      }
    }
    describeResultOf(Get("/topics/2344")) { op =>
      it("contains the user's id") {
        op should have (cid (userId))
      }
      it("selects the correct topic resource") {
        op should have (res (ResTopic("2344")))
      }
      it("selects the entire object") {
        op should have (opSpec (GetAll))
      }
    }
  }
}
