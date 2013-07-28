package chatless.db
import chatless._

import argonaut.Json

case class TopicModel(
    tid:TopicId,
    title:String,
    public:Boolean,
    info:Json,
    tags:Set[String],
    op:UserId,
    sops:Set[UserId],
    participating:Set[UserId])
  extends ModelAccess {
}
