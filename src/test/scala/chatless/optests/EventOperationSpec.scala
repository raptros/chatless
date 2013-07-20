package chatless.optests

import chatless.db._
import chatless.services.EventApi
import org.scalatest.FunSpec

import argonaut._
import Argonaut._

import chatless._
import chatless.operation._
import scala.Some

class EventOperationSpec extends FunSpec with ServiceSpecBase with EventApi {
  val apiInspector = eventApi { dbReq:Operation => complete { dbReq.asJson } }

  describe("the operation object returned when the /event/ api receives a") {
    describeResultOf(Get("/event")) {
      operationTest(ResEvents, GetRelative(false, None, true, 1))
    }
    describeResultOf(Get("/event/last")) {
      operationTest(ResEvents, GetRelative(false, None, true, 1))
    }

    describeResultOf(Get("/event/last/5")) {
      operationTest(ResEvents, GetRelative(false, None, true, 5))
    }

    describeResultOf(Get("/event/at/4ia324")) {
      operationTest(ResEvents, GetRelative(false, Some("4ia324"), true, 1))
    }

    describeResultOf(Get("/event/at/4ia324/5")) {
      operationTest(ResEvents, GetRelative(false, Some("4ia324"), true, 5))
    }

    describeResultOf(Get("/event/before/4ia324")) {
      operationTest(ResEvents, GetRelative(false, Some("4ia324"), false, 1))
    }

    describeResultOf(Get("/event/before/4ia324/5")) {
      operationTest(ResEvents, GetRelative(false, Some("4ia324"), false, 5))
    }

    describeResultOf(Get("/event/from/4ia324")) {
      operationTest(ResEvents, GetRelative(true, Some("4ia324"), true, 1))
    }

    describeResultOf(Get("/event/from/4ia324/5")) {
      operationTest(ResEvents, GetRelative(true, Some("4ia324"), true, 5))
    }

    describeResultOf(Get("/event/after/4ia324")) {
      operationTest(ResEvents, GetRelative(true, Some("4ia324"), false, 1))
    }

    describeResultOf(Get("/event/after/4ia324/5")) {
      operationTest(ResEvents, GetRelative(true, Some("4ia324"), false, 5))
    }
  }

}
