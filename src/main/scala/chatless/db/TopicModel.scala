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

  lazy val fields:Map[String, FieldAccess[_]] = Map(
    "tid" -> tid,
    "title" -> title,
    "public" -> public,
    "info" -> info,
    "tags" -> tags,
    "op" -> op,
    "sops" -> sops,
    "participating" -> participating
  )

  lazy val replacableFields = Map(
    "title" -> { (t:String) => copy(title = t) },
    "public" -> { (p:Boolean) => copy(public = p) },
    "info" -> { (i:Json) => copy(info = i) }
  )

  lazy val updatableListFields:Map[String, UpdateListField[_]] = Map(
    "sops" -> mkUpdateListField[UserId](u => copy(sops = sops + u), u => copy(sops = sops - u)),
    "tags" -> mkUpdateListField[String](t => copy(tags = tags + t), t => copy(tags = tags - t)),
    "participating" -> mkUpdateListField[UserId](
      u => copy(participating = participating + u),
      u => copy(participating = participating - u))
  )

  lazy val readPublic = Set("tid", "title", "public")

  def canRead(cid:UserId, field:String):Boolean =
    public || (readPublic contains field) || (op == cid) || (sops contains cid) || (participating contains cid)

  def canWrite(cid:UserId, field:String):Boolean = (op == cid) || (sops contains cid)
}
