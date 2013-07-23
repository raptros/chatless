package chatless.optests

import chatless.db._
import chatless.services.MeApi
import org.scalatest.FunSpec

import argonaut._
import Argonaut._

import chatless._
import chatless.operation._
import chatless.operation.ReplaceField
import chatless.operation.GetField

class MeApiOperationSpec extends FunSpec with ServiceSpecBase with MeApi {
  val apiInspector = meApi { dbReq:Operation => complete { dbReq.asJson } }

  describe("the operation object returned when the /me/ api receives a") {
    describeResultOf(Get("/me/")) { operationTest(ResMe, GetAll) }

    describeResultOf(Get("/me/nick")) { operationTest(ResMe, GetField("nick")) }

    describeResultOf(Put("/me/nick", "badger")) { operationTest(ResMe, ReplaceField("nick", StringVC("badger"))) }

    describeResultOf(Get("/me/public")) { operationTest(ResMe, GetField("public")) }

    describeResultOf(Put("/me/public", true)) { operationTest(ResMe, ReplaceField("public", BooleanVC(true))) }

    describeResultOf(Get("/me/info")) { operationTest(ResMe, GetField("info")) }

    describeResultOf(Put("/me/info", ("test" := 123) ->: jEmptyObject)) {
      operationTest(ResMe, ReplaceField("info", JsonVC(("test" := 123) ->: jEmptyObject)))
    }

    //following list
    describeResultOf(Get("/me/following")) {
      operationTest(ResMe, GetField("following"))
    }

    describeResultOf(Get("/me/following/3324")) {
      operationTest(ResMe, GetListContains("following", StringVC("3324")))
    }

    describeResultOf(Put("/me/following/3324"))  {
      operationTest(ResMe, AppendToList("following", StringVC("3324")))
    }

    describeResultOf(Delete("/me/following/3324")) {
      operationTest(ResMe, DeleteFromList("following", StringVC("3324")))
    }

    //followers list
    describeResultOf(Get("/me/followers")) {
      operationTest(ResMe, GetField("followers"))
    }

    describeResultOf(Get("/me/followers/3324")) {
      operationTest(ResMe, GetListContains("followers", StringVC("3324")))
    }

    //todo this should be changed to expect failure.
/*    describeResultOf(Put("/me/followers/3324"))  {
      operationTest(ResMe, AppendToList("followers", StringVC("3324")))
    }*/

    describeResultOf(Delete("/me/followers/3324")) {
      operationTest(ResMe, DeleteFromList("followers", StringVC("3324")))
    }

    //blocked list
    describeResultOf(Get("/me/blocked")) {
      operationTest(ResMe, GetField("blocked"))
    }

    describeResultOf(Get("/me/blocked/3324")) {
      operationTest(ResMe, GetListContains("blocked", StringVC("3324")))
    }

    describeResultOf(Put("/me/blocked/3324"))  {
      operationTest(ResMe, AppendToList("blocked", StringVC("3324")))
    }

    describeResultOf(Delete("/me/blocked/3324")) {
      operationTest(ResMe, DeleteFromList("blocked", StringVC("3324")))
    }

    //topics list
    describeResultOf(Get("/me/topics")) {
      operationTest(ResMe, GetField("topics"))
    }

    describeResultOf(Get("/me/topics/3324")) {
      operationTest(ResMe, GetListContains("topics", StringVC("3324")))
    }

    describeResultOf(Put("/me/topics/3324"))  {
      operationTest(ResMe, AppendToList("topics", StringVC("3324")))
    }

    describeResultOf(Delete("/me/topics/3324")) {
      operationTest(ResMe, DeleteFromList("topics", StringVC("3324")))
    }

    //tags list
    describeResultOf(Get("/me/tags")) {
      operationTest(ResMe, GetField("tags"))
    }

    describeResultOf(Get("/me/tags/3324")) {
      operationTest(ResMe, GetListContains("tags", StringVC("3324")))
    }

    describeResultOf(Put("/me/tags/3324"))  {
      operationTest(ResMe, AppendToList("tags", StringVC("3324")))
    }

    describeResultOf(Delete("/me/tags/3324")) {
      operationTest(ResMe, DeleteFromList("tags", StringVC("3324")))
    }

  }

  describe("the operation object returned when the /me/requests sub-api receives a") {
    describeResultOf(Get("/me/requests")) {
      operationTest(ResMeReqs, GetField("open"))
    }

    describeResultOf(Get("/me/request/3324")) {
      operationTest(ResRequest("3324"), GetAll)
    }

    describeResultOf(Get("/me/requests/accepted/")) {
      operationTest(ResMeReqs, GetField("accepted"))
    }

    describeResultOf(Get("/me/requests/accepted/3324"))  {
      operationTest(ResMeReqs, GetListContains("accepted", StringVC("3324")))
    }

    describeResultOf(Put("/me/requests/accepted/3324"))  {
      operationTest(ResMeReqs, AppendToList("accepted", StringVC("3324")))
    }

    describeResultOf(Get("/me/requests/rejected/")) {
      operationTest(ResMeReqs, GetField("rejected"))
    }

    describeResultOf(Get("/me/requests/rejected/3324"))  {
      operationTest(ResMeReqs, GetListContains("rejected", StringVC("3324")))
    }

    describeResultOf(Put("/me/requests/rejected/3324"))  {
      operationTest(ResMeReqs, AppendToList("rejected", StringVC("3324")))
    }
  }
}
