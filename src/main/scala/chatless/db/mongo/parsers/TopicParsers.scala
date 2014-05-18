package chatless.db.mongo.parsers

import com.osinka.subset._
import DocParser._
import argonaut._
import Argonaut._
import chatless.model.topic.{TopicMode, Topic}

trait TopicParsers { this: SomeFields =>
  implicit val topicModeParser = bool("muted") ~ bool("public") map {
    case m ~ p => TopicMode(muted = m, public = p)
  }

  val topicParser = server ~ user ~ id ~ str("banner") ~ get[Json]("info") ~ get[TopicMode]("mode") map {
    case s ~ u ~ i ~ b ~ info ~ mode => Topic(s, u, i, b, info, mode)
  }
}
