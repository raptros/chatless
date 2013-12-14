package chatless.db
import chatless._

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import chatless.wiring.params.TopicCollection
import chatless.model.Topic
import scalaz.\/
import scalaz.\/.fromTryCatch

class SalatTopicDAO @Inject()(
  @TopicCollection collection: MongoCollection)
  extends SalatDAO[Topic, String](collection)
  with TopicDAO
  with DAOHelpers {

  def get(id: TopicId) = findOneById(id)

  def saveNewTopic(topic: Topic) = fromTryCatch { insert(topic) } leftMap { t => t.getMessage } map { oId =>
    oId.nonEmpty && oId.get == topic.id
  }

}
