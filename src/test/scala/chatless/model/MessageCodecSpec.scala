package chatless.model

import argonaut._
import Argonaut._
import org.scalatest.{Matchers, FlatSpec}
import org.joda.time.DateTime
import chatless.model.topic.MemberMode

class MessageCodecSpec extends FlatSpec with Matchers {
  implicit def dateTimeEncodeJson = EncodeJson[DateTime] { dt => jString(dt.toString) }

  implicit def dateTimeDecodeJson = DecodeJson[DateTime] { c =>
    for {
      ts <- c.as[String]
      dt <- catchJodaParseFailure(c)(DateTime.parse(ts))
    } yield dt
  }

  private def catchJodaParseFailure(c: HCursor)(jOp: => DateTime): DecodeResult[DateTime] = try { okResult(jOp) } catch {
    case e: IllegalArgumentException => failResult(e.getMessage, c.history)
  }

  behavior of "the message codecs"

  val tc = TopicCoordinate("test", "model", "MessageCodecSpec")
  val uc = UserCoordinate("test", "poster")

  it should "encode a posted message properly" in {
    val m = MessageBuilder.at(tc.message("encodePosted"), DateTime.now()).posted(uc, jEmptyObject)
    val js = m.asJson
    val fields = js.objectFieldsOrEmpty
    fields should contain allOf ("server", "topic", "id", "poster", "body")
  }

  it should "decode a posted message properly" in {
    val m = MessageBuilder.at(tc.message("decodePosted"), DateTime.now()).posted(uc, jEmptyObject)
    val json = m.asJson
    println(json.spaces2)
    val decoded = json.jdecode[Message] getOr fail(s"could not decode this json: ${json.spaces2}")
    decoded shouldBe a [PostedMessage]
  }

  it should "decode a user joined message properly" in {
    val m = MessageBuilder.at(tc.message("decodeUserJoined"), DateTime.now()).userJoined(uc, MemberMode(true, true, true))
    val json = m.asJson
    println(json.spaces2)
    val decoded = json.jdecode[Message] getOr fail(s"could not decode this json: ${json.spaces2}")
    decoded shouldBe a [UserJoinedMessage]
  }

  it should "decode a banner changed message properly" in {
    val m = MessageBuilder.at(tc.message("decodeBannerChanged"), DateTime.now()).bannerChanged(uc, "new banner")
    val json = m.asJson
    println(json.spaces2)
    val decoded = json.jdecode[Message] getOr fail(s"could not decode this json: ${json.spaces2}")
    decoded shouldBe a [BannerChangedMessage]
  }

}
