package chatless.model
import argonaut._
import Argonaut._
import org.scalatest.{Matchers, FlatSpec}
import chatless.model.topic.{TopicMode, TopicInit}

class TopicInitCodecSpec extends FlatSpec with Matchers with TopicInitMatchers {
  behavior of "the topic init codec"

  val init1 =
    """
      |{
      | "fix-id": "wtf",
      | "banner": "none",
      | "info": {"yo": 33},
      | "mode": {"public": false, "muted": true, "open": false},
      | "invite": [
      | {"server": "a", "user": "one"},
      | {"server": "b", "user": "two"},
      | {"server": "a", "user": "three"}
      | ]
      |}
    """.stripMargin
  val init2 =
    """
      |{
      | "banner": "none",
      | "info": {"yo": 33},
      | "invite": [
      | {"server": "a", "user": "one"},
      | {"server": "b", "user": "two"},
      | {"server": "a", "user": "three"}
      | ]
      |}
    """.stripMargin

  val init3 =
    """
      |{
      | "banner": "none",
      | "info": {"yo": 33}
      |}
    """.stripMargin

  val init4 =
    """
      |{
      | "banner": "none"
      |}
    """.stripMargin
  val init5 =
    """
      |{
      |}
    """.stripMargin

  it should "read all the fields" in {
    Parse.decodeEither[TopicInit](init1) valueOr { s => fail(s"parse error: $s") } should have (
      fixedId (Some("wtf")),
      banner ("none"),
      info (("yo" := 33) ->: jEmptyObject),
      invite (UserCoordinate("a", "one") ::
        UserCoordinate("b", "two") ::
        UserCoordinate("a", "three") ::
        Nil),
      mode (TopicMode(muted = true, open = false, public = false))
    )
  }

  it should "read 3 fields successfully" in {
    Parse.decodeEither[TopicInit](init2) valueOr { s => fail(s"parse error: $s") } should have (
      fixedId (None),
      banner ("none"),
      info (("yo" := 33) ->: jEmptyObject),
      invite (UserCoordinate("a", "one") ::
        UserCoordinate("b", "two") ::
        UserCoordinate("a", "three") ::
        Nil),
      mode (TopicMode(muted = false, open = true, public = false))
    )
  }

  it should "read just banner and info" in {
    Parse.decodeEither[TopicInit](init3) valueOr { s => fail(s"parse error: $s") } should have (
      fixedId (None),
      banner ("none"),
      info (("yo" := 33) ->: jEmptyObject),
      invite (Nil),
      mode (TopicMode(muted = false, open = true, public = false))
    )
  }

  it should "read just banner" in {
    Parse.decodeEither[TopicInit](init4) valueOr { s => fail(s"parse error: $s") } should have (
      fixedId (None),
      banner ("none"),
      info (jEmptyObject),
      invite (Nil),
      mode (TopicMode(muted = false, open = true, public = false))
    )
  }

  it should "read an empty object" in {
    init5.decodeEither[TopicInit] valueOr { s => fail(s"parse error: $s") } should have (
      fixedId (None),
      banner (""),
      info (jEmptyObject),
      invite (Nil),
      mode (TopicMode(muted = false, open = true, public = false))
    )
  }

  it should "write out a certain string" in {
    val json = TopicInit().asJson.nospaces
    json should not include "fix-id"
    json should include ("banner")
  }

  it should "ignore unknown fields" in {
    """
      |{
      | "banner": "yo",
      | "junk": 444
      |}
    """.stripMargin.decodeEither[TopicInit] valueOr { s => fail(s"parse error: $s") } should have (
      fixedId (None),
      banner ("yo"),
      info (jEmptyObject),
      invite (Nil),
      mode (TopicMode(muted = false, open = true, public = false))
    )
  }
}
