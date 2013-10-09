package chatless.model

import chatless._
import org.json4s._
import com.novus.salat.annotations._

case class Topic(
    @Key("_id") id: TopicId,
    title: String,
    public: Boolean,
    muted: Boolean,
    info: JDoc,
    op: UserId,
    sops: Set[UserId],
    voiced: Set[UserId],
    participating: Set[UserId],
    banned: Set[UserId],
    tags: Set[String]) {
  import Topic._

  def getFields(fields: Set[String]): Map[String, Any] = {
    (ID -> id) ::
    (TITLE -> title) ::
    (PUBLIC -> public) ::
    (MUTED -> muted) ::
    (INFO -> info) ::
    (OP -> op) ::
    (SOPS -> sops) ::
    (VOICED -> voiced) ::
    (PARTICIPATING -> participating) ::
    (BANNED -> banned) ::
    (TAGS -> tags) ::
    Nil
  }.toMap filterKeys { fields.contains }
}

object Topic {
  val ID = "id"
  val TITLE = "title"
  val PUBLIC = "public"
  val MUTED = "muted"
  val INFO = "info"
  val OP = "op"
  val SOPS = "sops"
  val VOICED = "voiced"
  val PARTICIPATING = "participating"
  val BANNED = "banned"
  val TAGS = "tags"

  val publicFields = {
    ID :: TITLE :: PUBLIC :: MUTED :: Nil
  }.toSet

  val participantFields = {
    ID :: TITLE :: PUBLIC :: MUTED :: INFO :: OP :: SOPS :: VOICED :: PARTICIPATING :: BANNED :: TAGS :: Nil
  }.toSet

}

