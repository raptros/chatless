package chatless.db.mongo.builders

import chatless.model.Topic
import chatless.db.mongo.Fields
import argonaut._
import Argonaut._

trait TopicWriter { this: BasicWritables =>

  implicit val topicWritesToDBO = WritesToDBO[Topic] { topic =>
    DBO2(
      //ensure that _id collision has exactly the same behavior as coordinate collision
      Fields._id --> (topic.server + topic.user + topic.id),
      Fields.server --> topic.server,
      Fields.user --> topic.user,
      Fields.id --> topic.id,
      Fields.banner --> topic.banner,
      Fields.info --> topic.info.asJson.nospaces
    )
  }

}
