package chatless.model

import chatless._
import org.json4s._
import com.novus.salat.annotations._

case class Topic(
    @Key("_id") id: TopicId,
    title: String,
    public: Boolean,
    info: JDoc,
    op: UserId,
    sops: Set[UserId],
    participating: Set[UserId],
    tags: Set[String]) {
  import Topic._

  def getFields(fields: Set[String]): Map[String, Any] = {
    (ID -> id) ::
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
  val ID = "id"
  val TITLE = "title"
  val PUBLIC = "public"
  val INFO = "info"
  val OP = "op"
  val SOPS = "sops"
  val PARTICIPATING = "participating"
  val TAGS = "tags"

  val publicFields = {
    ID :: TITLE :: PUBLIC :: Nil
  }.toSet

  val participantFields = {
    ID :: TITLE :: PUBLIC :: INFO :: OP :: SOPS :: PARTICIPATING :: TAGS :: Nil
  }.toSet

}

