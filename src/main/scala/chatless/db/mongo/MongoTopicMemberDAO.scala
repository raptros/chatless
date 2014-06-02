package chatless.db.mongo

import com.google.inject.Inject
import chatless.wiring.params.TopicMemberCollection
import com.mongodb.casbah.Imports._
import io.github.raptros.bson.Bson._
import codecs.Codecs._
import chatless.db._
import chatless.model.{Coordinate, UserCoordinate, TopicCoordinate}
import chatless.model.topic.{Member, MemberMode}
import scalaz._
import scalaz.\/._
import scalaz.OptionT._
import scalaz.syntax.id._
import scalaz.syntax.monad._
import scalaz.syntax.traverse._
import scalaz.std.list._
import com.typesafe.scalalogging.slf4j.LazyLogging
import chatless.db.WriteFailureWithCoordinate

import MongoSafe.{StringAsLocation, writeMongo}

class MongoTopicMemberDAO @Inject() (@TopicMemberCollection val collection: MongoCollection)
  extends TopicMemberDAO
  with LazyLogging
  with MongoSafe {

  def get(topic: TopicCoordinate, user: UserCoordinate) = innerGet(topic, user).run

  def innerGet(topic: TopicCoordinate, user: UserCoordinate): OptionT[DbResult, Member] = for {
    res <- optionT[DbResult](safeFindOne("member" atCoord topic)(DBO("topic" :> topic, "user" :> user)))
    decoded = res.decode[Member] leftMap wrapDecodeErrors("member", topic)
    member <- decoded.liftM[OptionT]
  } yield member


  def set(topic: TopicCoordinate, user: UserCoordinate, mode: MemberMode): DbResult[Member] =
    writeMongo("member" atCoord topic) {
      collection.update(
        q = DBO("topic" :> topic, "user" :> user),
        o = $set("mode" -> mode.asBson),
        upsert = true)
      Member(topic, user, mode)
    }

  def list(topic: TopicCoordinate): DbResult[Seq[Member]] = for {
    items <- safeFindList("members" atCoord topic)(DBO("topic" :> topic))
    results <- items.traverse[DbResult, Member] { _.decode[Member] leftMap wrapDecodeErrors("member", topic) }
  } yield results

  private def setup() {
    logger.debug("setup()")
    collection.ensureIndex(
      keys = DBO("topic.server" :> 1, "topic.user" :> 1, "topic.topic" :> 1, "user.server" :> 1, "user.user" :> 1),
      options = DBO("unique" :> true)
    )
  }

  setup()
}
