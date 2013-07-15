import chatless.db._
import chatless.db.GetFields
import chatless.db.ReplaceField
import chatless.db.ResUser
import chatless.services.MeApi
import org.scalatest.FunSpec

import argonaut._
import Argonaut._

import chatless._

class MeRoutesSpec extends FunSpec with ServiceSpecBase with MeApi {
  val apiInspector = meApi { dbReq:Operation => complete { dbReq.asJson } }

  describe("the operation object returned when the /me/ api receives a") {
    describeResultOf(Get("/me/")) { operationTest(ResUser(userId), GetAll) }

    describeResultOf(Get("/me/nick")) { operationTest(ResUser(userId), GetFields("nick")) }

    describeResultOf(Put("/me/nick", "badger")) { operationTest(ResUser(userId), ReplaceField("nick", StringVC("badger"))) }

    describeResultOf(Get("/me/public")) { operationTest(ResUser(userId), GetFields("public")) }

    describeResultOf(Put("/me/public", true)) { operationTest(ResUser(userId), ReplaceField("public", BooleanVC(true))) }

    describeResultOf(Get("/me/info")) { operationTest(ResUser(userId), GetFields("info")) }

    describeResultOf(Put("/me/info", ("test" := 123) ->: jEmptyObject)) {
      operationTest(ResUser(userId), ReplaceField("info", JsonVC(("test" := 123) ->: jEmptyObject)))
    }

    //following list
    describeResultOf(Get("/me/following")) {
      operationTest(ResUser(userId), GetFields("following"))
    }

    describeResultOf(Get("/me/following/3324")) {
      operationTest(ResUser(userId), GetListContains("following", StringVC("3324")))
    }

    describeResultOf(Put("/me/following/3324"))  {
      operationTest(ResUser(userId), AppendToList("following", StringVC("3324")))
    }

    describeResultOf(Delete("/me/following/3324")) {
      operationTest(ResUser(userId), DeleteFromList("following", StringVC("3324")))
    }

    //followers list
    describeResultOf(Get("/me/followers")) {
      operationTest(ResUser(userId), GetFields("followers"))
    }

    describeResultOf(Get("/me/followers/3324")) {
      operationTest(ResUser(userId), GetListContains("followers", StringVC("3324")))
    }

    //todo this should be changed to expect failure.
/*    describeResultOf(Put("/me/followers/3324"))  {
      operationTest(ResUser(userId), AppendToList("followers", StringVC("3324")))
    }*/

    describeResultOf(Delete("/me/followers/3324")) {
      operationTest(ResUser(userId), DeleteFromList("followers", StringVC("3324")))
    }

    //blocked list
    describeResultOf(Get("/me/blocked")) {
      operationTest(ResUser(userId), GetFields("blocked"))
    }

    describeResultOf(Get("/me/blocked/3324")) {
      operationTest(ResUser(userId), GetListContains("blocked", StringVC("3324")))
    }

    describeResultOf(Put("/me/blocked/3324"))  {
      operationTest(ResUser(userId), AppendToList("blocked", StringVC("3324")))
    }

    describeResultOf(Delete("/me/blocked/3324")) {
      operationTest(ResUser(userId), DeleteFromList("blocked", StringVC("3324")))
    }

    //topics list
    describeResultOf(Get("/me/topics")) {
      operationTest(ResUser(userId), GetFields("topics"))
    }

    describeResultOf(Get("/me/topics/3324")) {
      operationTest(ResUser(userId), GetListContains("topics", StringVC("3324")))
    }

    describeResultOf(Put("/me/topics/3324"))  {
      operationTest(ResUser(userId), AppendToList("topics", StringVC("3324")))
    }

    describeResultOf(Delete("/me/topics/3324")) {
      operationTest(ResUser(userId), DeleteFromList("topics", StringVC("3324")))
    }

    //tags list
    describeResultOf(Get("/me/tags")) {
      operationTest(ResUser(userId), GetFields("tags"))
    }

    describeResultOf(Get("/me/tags/3324")) {
      operationTest(ResUser(userId), GetListContains("tags", StringVC("3324")))
    }

    describeResultOf(Put("/me/tags/3324"))  {
      operationTest(ResUser(userId), AppendToList("tags", StringVC("3324")))
    }

    describeResultOf(Delete("/me/tags/3324")) {
      operationTest(ResUser(userId), DeleteFromList("tags", StringVC("3324")))
    }

  }

  describe("the operation object returned when the /me/requests sub-api receives a") {
    //requests list ... maybe this needs to be split off into a separate module?
    describeResultOf(Get("/me/requests")) {
      operationTest(ResUserReqs(userId), GetFields("open"))
    }

    describeResultOf(Get("/me/requests/3324")) {
      operationTest(ResRequest("3324"), GetAll)
    }

    describeResultOf(Get("/me/requests/accepted/")) {
      operationTest(ResUserReqs(userId), GetFields("accepted"))
    }

    describeResultOf(Get("/me/requests/accepted/3324"))  {
      operationTest(ResUserReqs(userId), GetListContains("accepted", StringVC("3324")))
    }

    describeResultOf(Put("/me/requests/accepted/3324"))  {
      operationTest(ResUserReqs(userId), AppendToList("accepted", StringVC("3324")))
    }

    describeResultOf(Get("/me/requests/rejected/")) {
      operationTest(ResUserReqs(userId), GetFields("rejected"))
    }

    describeResultOf(Get("/me/requests/rejected/3324"))  {
      operationTest(ResUserReqs(userId), GetListContains("rejected", StringVC("3324")))
    }

    describeResultOf(Put("/me/requests/rejected/3324"))  {
      operationTest(ResUserReqs(userId), AppendToList("rejected", StringVC("3324")))
    }
  }
}
