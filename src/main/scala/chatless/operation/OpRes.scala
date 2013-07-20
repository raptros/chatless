package chatless.operation

import chatless.{UserId, TopicId, RequestId}
import argonaut._
import Argonaut._

sealed abstract class OpRes {
   def asJson:Json = jEmptyObject
 }

object OpRes {
  implicit def OpResEncodeJ:EncodeJson[OpRes] = EncodeJson { _.asJson }

  implicit def JDecodeOpRes:DecodeJson[OpRes] = DecodeJson { c =>
    (c --\ "res").as[String] flatMap {
      case "me" => okResult { ResMe }
      case "user" => (c --\ "uid").as[String] map { ResUser }
      case "topic" => (c --\ "tid").as[String] map { ResTopic }
      case "req" => (c --\ "rid").as[String] map { ResRequest }
      case "mreqs" => okResult { ResMeReqs }
      case "ureqs" => (c --\ "uid").as[String] map { ResUserReqs }
      case "treqs" => (c --\ "tid").as[String] map { ResTopicReqs }
      case "msgs" => (c --\ "tid").as[String] map { ResMessages }
      case "events" => okResult { ResEvents }
      case "tag" => (c --\ "tag").as[String] map { ResTagged }
      case _ => DecodeResult.fail("not a valid resource spec", c.history)
    }
  }
}

case object ResMe extends OpRes {
  override def asJson:Json = ("res" := "me") ->: super.asJson
}

case class ResUser(uid:UserId) extends OpRes {
  override def asJson:Json = ("res" := "user") ->: ("uid" := uid) ->: super.asJson
}

case class ResTopic(tid:TopicId) extends OpRes {
  override def asJson:Json = ("res" := "topic") ->: ("tid" := tid) ->: super.asJson
}

case class ResRequest(rid:RequestId) extends OpRes {
  override def asJson:Json = ("res" := "req") ->: ("rid" := rid) ->: super.asJson
}

case object ResMeReqs extends OpRes {
  override def asJson:Json = ("res" := "mreqs") ->: super.asJson
}
case class ResUserReqs(uid:UserId) extends OpRes {
  override def asJson:Json = ("res" := "ureqs") ->: ("uid" := uid) ->: super.asJson
}

case class ResTopicReqs(tid:TopicId) extends OpRes {
  override def asJson:Json = ("res" := "treqs") ->: ("tid" := tid) ->: super.asJson
}

case class ResMessages(tid:TopicId) extends OpRes {
  override def asJson:Json = ("res" := "msgs") ->: ("tid" := tid) ->: super.asJson
}

case object ResEvents extends OpRes {
  override def asJson:Json = ("res" := "events") ->: super.asJson
}

case class ResTagged(tag:String) extends OpRes {
  override def asJson:Json = ("res" := "tag") ->: ("tag" := tag) ->: super.asJson
}
