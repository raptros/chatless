import chatless.db._
import chatless.services.TopicApi
import chatless._
import org.scalatest.{FunSpec, WordSpec}
import spray.httpx.marshalling.Marshaller._

import argonaut._
import Argonaut._


class TopicsRouteSpec extends FunSpec with ServiceSpecBase with TopicApi {
  val apiInspector = topicApi { dbReq => complete { dbReq.asJson } }

  describe("the operation object returned when the topic api receives a") {
    describeResultOf(Get("/topic/2344")) {
      operationTest(ResTopic("2344"), GetAll)
    }

    describeResultOf(Get("/topic/2344/title")) {
      operationTest(ResTopic("2344"), GetFields("title"))
    }

    describeResultOf(Put("/topic/2344/title", "this is a topic")) {
      operationTest(ResTopic("2344"), ReplaceField("title", StringVC("this is a topic")))
    }

    describeResultOf(Get("/topic/2344/public")) {
      operationTest(ResTopic("2344"), GetFields("public"))
    }

    describeResultOf(Put("/topic/2344/public", true)) {
      operationTest(ResTopic("2344"), ReplaceField("public", BooleanVC(true)) )
    }

    describeResultOf(Put("/topic/2344/public", false)) {
      operationTest(ResTopic("2344"), ReplaceField("public", BooleanVC(false)))
    }

    describeResultOf(Get("/topic/2344/info")) {
      operationTest(ResTopic("2344"), GetFields("info"))
    }

    describeResultOf(Put("/topic/2344/info", ("test" := 123) ->: jEmptyObject)) {
      operationTest(ResTopic("2344"), ReplaceField("info", JsonVC(("test" := 123) ->: jEmptyObject)))
    }

    describeResultOf(Get("/topic/2344/tags")) {
      operationTest(ResTopic("2344"), GetFields("tags"))
    }

    describeResultOf(Get("/topic/2344/tags/i'll_do_yourm_other")) {
      operationTest(ResTopic("2344"), GetListContains("tags", StringVC("i'll_do_yourm_other")))
    }

    describeResultOf(Put("/topic/2344/tags/i'll_do_yourm_other")) {
      operationTest(ResTopic("2344"), AppendToList("tags", StringVC("i'll_do_yourm_other")))
    }

    describeResultOf(Delete("/topic/2344/tags/i'll_do_yourm_other")) {
      operationTest(ResTopic("2344"), DeleteFromList("tags", StringVC("i'll_do_yourm_other")))
    }

    describeResultOf(Get("/topic/2344/op")) {
      operationTest(ResTopic("2344"), GetFields("op"))
    }

    describeResultOf(Get("/topic/2344/sops")) {
      operationTest(ResTopic("2344"), GetFields("sops"))
    }

    describeResultOf(Get("/topic/2344/sops/23423")) {
      operationTest(ResTopic("2344"), GetListContains("sops", StringVC("23423")))
    }

    describeResultOf(Put("/topic/2344/sops/23423")) {
      operationTest(ResTopic("2344"), AppendToList("sops", StringVC("23423")))
    }

    describeResultOf(Delete("/topic/2344/sops/23423")) {
      operationTest(ResTopic("2344"), DeleteFromList("sops", StringVC("23423")))
    }

    describeResultOf(Get("/topic/2344/participating")) {
      operationTest(ResTopic("2344"), GetFields("participating"))
    }

    describeResultOf(Get("/topic/2344/participating/23423")) {
      operationTest(ResTopic("2344"), GetListContains("participating", StringVC("23423")))
    }

    describeResultOf(Put("/topic/2344/participating/23423")) {
      operationTest(ResTopic("2344"), AppendToList("participating", StringVC("23423")))
    }

    describeResultOf(Delete("/topic/2344/participating/23423")) {
      operationTest(ResTopic("2344"), DeleteFromList("participating", StringVC("23423")))
    }
  }

  describe("the operation object returned when the /topic/:id/requests sub-api receives a") {
    describeResultOf(Get("/topic/2344/requests")) {
      operationTest(ResTopicReqs("2344"), GetFields("open"))
    }

    describeResultOf(Post("/topic/2344/requests", ("sender" := userId) ->: jEmptyObject)) {
      operationTest(ResTopicReqs("2344"), Create(JsonVC(("sender" := userId) ->: jEmptyObject)))
    }

    describeResultOf(Get("/topic/2344/request/3324")) {
      operationTest(ResRequest("3324"), GetAll)
    }

    describeResultOf(Get("/topic/2344/requests/accepted/")) {
      operationTest(ResTopicReqs("2344"), GetFields("accepted"))
    }

    describeResultOf(Get("/topic/2344/requests/accepted/3324"))  {
      operationTest(ResTopicReqs("2344"), GetListContains("accepted", StringVC("3324")))
    }

    describeResultOf(Put("/topic/2344/requests/accepted/3324"))  {
      operationTest(ResTopicReqs("2344"), AppendToList("accepted", StringVC("3324")))
    }

    describeResultOf(Get("/topic/2344/requests/rejected/")) {
      operationTest(ResTopicReqs("2344"), GetFields("rejected"))
    }

    describeResultOf(Get("/topic/2344/requests/rejected/3324"))  {
      operationTest(ResTopicReqs("2344"), GetListContains("rejected", StringVC("3324")))
    }

    describeResultOf(Put("/topic/2344/requests/rejected/3324"))  {
      operationTest(ResTopicReqs("2344"), AppendToList("rejected", StringVC("3324")))
    }
  }
}
