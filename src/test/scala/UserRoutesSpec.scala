import chatless.db._
import chatless.db.GetFields
import chatless.db.ResUser
import chatless.services.UserApi
import org.scalatest.{FunSpec, FunSuite}
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest
import argonaut._
import Argonaut._
import chatless._

class UserRoutesSpec extends FunSpec with ServiceSpecBase with UserApi {
  val apiInspector = userApi { dbReq:Operation => complete { dbReq.asJson } }

  describe("the operation object returned when the /me/ api receives a") {
    describeResultOf(Get("/user/555/")) { operationTest(ResUser("555"), GetAll) }

    describeResultOf(Get("/user/555/nick")) { operationTest(ResUser("555"), GetFields("nick")) }

    describeResultOf(Get("/user/555/public")) { operationTest(ResUser("555"), GetFields("public")) }

    describeResultOf(Get("/user/555/info")) { operationTest(ResUser("555"), GetFields("info")) }

    //following list
    describeResultOf(Get("/user/555/following")) {
      operationTest(ResUser("555"), GetFields("following"))
    }

    describeResultOf(Get("/user/555/following/3324")) {
      operationTest(ResUser("555"), GetListContains("following", StringVC("3324")))
    }

    //followers list
    describeResultOf(Get("/user/555/followers")) {
      operationTest(ResUser("555"), GetFields("followers"))
    }

    describeResultOf(Get("/user/555/followers/3324")) {
      operationTest(ResUser("555"), GetListContains("followers", StringVC("3324")))
    }

    //topics list
    describeResultOf(Get("/user/555/topics")) {
      operationTest(ResUser("555"), GetFields("topics"))
    }

    describeResultOf(Get("/user/555/topics/3324")) {
      operationTest(ResUser("555"), GetListContains("topics", StringVC("3324")))
    }
  }

  describe("the operation object returned when the /user/555/requests sub-api receives a") {
    describeResultOf(Get("/user/555/request/3324")) {
      operationTest(ResRequest("3324"), GetAll)
    }

    describeResultOf(Post("/user/555/requests", ("sender" := userId) ->: jEmptyObject)) {
      operationTest(ResUserReqs("555"), Create(JsonVC(("sender" := userId) ->: jEmptyObject)))
    }
  }

}