package chatless.model

import chatless._
import org.json4s._

case class Topic(
    tid: TopicId,
    title: String,
    public: Boolean,
    info: JObject,
    op: UserId,
    sops: Set[UserId],
    participating: Set[UserId],
    tags: Set[String])
  extends BaseModel {
  import Topic._

  def getFields(fields: Set[String]): Map[String, Any] = {
    (TID -> tid) ::
    (TITLE -> title) ::
    (PUBLIC -> public) ::
    (INFO -> info) ::
    (OP -> op) ::
    (SOPS -> sops) ::
    (PARTICIPATING -> participating) ::
    (TAGS -> tags) ::
    Nil
  }.toMap filterKeys { fields.contains }
}

object Topic {
  val TID = "name"
  val TITLE = "title"
  val PUBLIC = "public"
  val INFO = "info"
  val OP = "op"
  val SOPS = "sops"
  val PARTICIPATING = "participating"
  val TAGS = "tags"

  val publicFields = TID :: TITLE :: PUBLIC :: Nil
  val participantFields = TID :: TITLE :: PUBLIC :: INFO :: OP :: SOPS :: PARTICIPATING :: TAGS :: Nil

}

