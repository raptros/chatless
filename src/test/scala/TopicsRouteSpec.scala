import chatless.db._
import chatless.services.Topics
import chatless._
import org.scalatest.{FunSpec, WordSpec}
import spray.httpx.marshalling.Marshaller._

import argonaut._
import Argonaut._


class TopicsRouteSpec extends FunSpec with ServiceSpecBase with Topics {
  val apiInspector = topicsApi { dbReq => complete { dbReq.asJson } }

  describe("the operation object returned when the topics api receives a") {
    describeResultOf(Get("/topics/2344")) {
      operationTest(ResTopic("2344"), GetAll)
    }

    describeResultOf(Get("/topics/2344/public")) {
      operationTest(ResTopic("2344"), GetFields("public"))
    }

/*    describeResultOf(Put("/topics/2344/public", true)) {
      operationTest(ResTopic("2344"), ReplaceField("public", BooleanVC(true)) )
    }*/

/*    describeResultOf(Put("/topics/2344/public", false)) {
      operationTest(ResTopic("2344"), ReplaceField("public", BooleanVC(false)))
    }*/

    describeResultOf(Get("/topics/2344/title")) {
      operationTest(ResTopic("2344"), GetFields("title"))
    }

  }
}
