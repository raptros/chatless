package chatless.db.mongo.parsers
import chatless.model._

import com.osinka.subset._
import DocParser._
import argonaut._
import Argonaut._

trait TopicParsers { this: SomeFields =>
  val topicParser = server ~ user ~ id ~ str("banner") ~ get[Json]("info") map {
    case s ~ u ~ i ~ b ~ info => Topic(s, u, i, b, info)
  }
}
