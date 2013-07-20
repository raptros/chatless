package chatless.optests

import chatless.services.TaggedApi
import org.scalatest.FunSpec

import argonaut._
import Argonaut._

import chatless._
import chatless.operation._

class TaggedApiOperationSpec extends FunSpec with ServiceSpecBase with TaggedApi {

  val apiInspector = taggedApi { operation:Operation => complete { operation.asJson } }

  describe("the operation object returned when the /tagged/some_tag_name/ api receives a") {
    describeResultOf(Get("/tagged/some_tag_name")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(false, None, true, 1))
    }
    describeResultOf(Get("/tagged/some_tag_name/last")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(false, None, true, 1))
    }

    describeResultOf(Get("/tagged/some_tag_name/last/5")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(false, None, true, 5))
    }

    describeResultOf(Get("/tagged/some_tag_name/at/4ia324")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(false, Some("4ia324"), true, 1))
    }

    describeResultOf(Get("/tagged/some_tag_name/at/4ia324/5")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(false, Some("4ia324"), true, 5))
    }

    describeResultOf(Get("/tagged/some_tag_name/before/4ia324")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(false, Some("4ia324"), false, 1))
    }

    describeResultOf(Get("/tagged/some_tag_name/before/4ia324/5")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(false, Some("4ia324"), false, 5))
    }

    describeResultOf(Get("/tagged/some_tag_name/from/4ia324")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(true, Some("4ia324"), true, 1))
    }

    describeResultOf(Get("/tagged/some_tag_name/from/4ia324/5")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(true, Some("4ia324"), true, 5))
    }

    describeResultOf(Get("/tagged/some_tag_name/after/4ia324")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(true, Some("4ia324"), false, 1))
    }

    describeResultOf(Get("/tagged/some_tag_name/after/4ia324/5")) {
      operationTest(ResTagged("some_tag_name"), GetRelative(true, Some("4ia324"), false, 5))
    }
  }

}
