package chatless.db.daos

import chatless._
import chatless.models.TopicM
import com.mongodb.casbah.Imports._

import scalaz._
import scalaz.std.option._
import scalaz.syntax.apply._
import scalaz.syntax.applicative._
import scalaz.syntax.std.option._
import scalaz.syntax.validation._
import shapeless._
import com.google.inject.Inject
import chatless.wiring.params.TopicCollection


class MongoTopicDAO @Inject() (@TopicCollection val topicCollection: MongoCollection) extends TopicDAO {
  def get(id: TopicId): Option[TopicM] = ???
}
