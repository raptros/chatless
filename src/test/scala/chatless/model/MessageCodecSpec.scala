package chatless.model

import argonaut._
import Argonaut._
import org.scalatest.{Matchers, FlatSpec}
import org.joda.time.DateTime
import chatless.model.topic.MemberMode
import chatless.model.ids._

import chatless.services.dateTimeEncodeJson
import chatless.services.dateTimeDecodeJson


class MessageCodecSpec extends FlatSpec with Matchers {
  behavior of "the message codecs"

  val tc = TopicCoordinate("test".serverId, "model".userId, "MessageCodecSpec".topicId)
  val uc = UserCoordinate("test".serverId, "poster".userId)

  it should "encode a posted message properly" in {
    val m = MessageBuilder.at(tc.message("encodePosted".messageId), DateTime.now()).posted(uc, jEmptyObject)
    val js = m.asJson
    val fields = js.objectFieldsOrEmpty
    fields should contain allOf ("server", "topic", "id", "poster", "body")
  }

  it should "decode a posted message properly" in {
    val m = MessageBuilder.at(tc.message("decodePosted".messageId), DateTime.now()).posted(uc, jEmptyObject)
    val json = m.asJson
    println(json.spaces2)
    val decoded = json.jdecode[Message] getOr fail(s"could not decode this json: ${json.spaces2}")
    decoded shouldBe a [PostedMessage]
  }

  it should "decode a user joined message properly" in {
    val m = MessageBuilder.at(tc.message("decodeUserJoined".messageId), DateTime.now()).userJoined(uc, MemberMode.creator)
    val json = m.asJson
    println(json.spaces2)
    val decoded = json.jdecode[Message] getOr fail(s"could not decode this json: ${json.spaces2}")
    decoded shouldBe a [UserJoinedMessage]
  }

  it should "decode a banner changed message properly" in {
    val m = MessageBuilder.at(tc.message("decodeBannerChanged".messageId), DateTime.now()).bannerChanged(uc, "new banner")
    val json = m.asJson
    println(json.spaces2)
    val decoded = json.jdecode[Message] getOr fail(s"could not decode this json: ${json.spaces2}")
    decoded shouldBe a [BannerChangedMessage]
  }

}
