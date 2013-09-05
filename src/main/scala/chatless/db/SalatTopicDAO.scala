package chatless.db
import chatless._

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import chatless.wiring.params.TopicCollection
import chatless.model.Topic

class SalatTopicDAO @Inject()(
  @TopicCollection collection: MongoCollection)
  extends SalatDAO[Topic, String](collection)
  with TopicDAO
  with DAOHelpers {

  def get(id: TopicId) = findOneById(id)

}
