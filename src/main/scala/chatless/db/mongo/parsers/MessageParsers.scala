package chatless.db.mongo.parsers
import chatless.model._

import com.osinka.subset._
import DocParser._
import argonaut._
import Argonaut._

trait MessageParsers { this: SomeFields with CoordinateParsers =>

  val messageBuilderParser = server ~ user ~ topic ~ id ~ timestamp map {
    case s ~ u ~ t ~ i ~ ts => MessageBuilder(s, u, t, i, ts)
  }

  type MessageBuilding = DocParser[MessageBuilder => Message]

  val postedMessageParser: MessageBuilding = poster ~ get[Json]("body") map {
    case p ~ b => _.posted(p, b)
  }

  val bannerChangedMessageParser: MessageBuilding = poster ~ str("banner") map {
    case p ~ b => _.bannerChanged(p, b)
  }

  val userJoinedMessageParser: MessageBuilding = get[UserCoordinate]("joined") map { uc => _ userJoined uc }

  val messageParser = for {
    builder <- messageBuilderParser
    parser <- postedMessageParser | bannerChangedMessageParser | userJoinedMessageParser
  } yield parser(builder)
}
