package chatless.model

import chatless._
import org.json4s._
import com.novus.salat.annotations._

case class Topic(
    @Key("_id")
    id:     TopicId,
    title:  String,
    public: Boolean,
    muted:  Boolean,
    info:   JDoc,
    op:     UserId,
    sops:   Set[UserId],
    voiced: Set[UserId],
    users:  Set[UserId],
    banned: Set[UserId],
    tags:   Set[String]) {
  import Topic._

  def getFields(fields: Set[String]): Map[String, Any] = Map(
    ID     -> id,
    TITLE  -> title,
    PUBLIC -> public,
    MUTED  -> muted,
    INFO   -> info,
    OP     -> op,
    SOPS   -> sops,
    VOICED -> voiced,
    USERS  -> users,
    BANNED -> banned,
    TAGS   -> tags
  ) filterKeys { fields.contains }
}

object Topic {
  val ID      = "id"
  val TITLE   = "title"
  val PUBLIC  = "public"
  val MUTED   = "muted"
  val INFO    = "info"
  val OP      = "op"
  val SOPS    = "sops"
  val VOICED  = "voiced"
  val USERS   = "users"
  val BANNED  = "banned"
  val TAGS    = "tags"

  val publicFields = {
    ID :: TITLE :: PUBLIC :: MUTED :: Nil
  }.toSet

  val userFields = {
    ID :: TITLE :: PUBLIC :: MUTED :: INFO :: OP :: SOPS :: VOICED :: USERS :: BANNED :: TAGS :: Nil
  }.toSet

  implicit val containableTopic = new ContainableValue[Topic] {
    def contain(a: Topic): ValueContainer = TopicVC(a)
  }
}

