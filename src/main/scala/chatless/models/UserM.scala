package chatless.models

import chatless._
import scala.reflect.runtime.universe._
import com.mongodb.casbah.Imports._

case class UserM(
    uid: UserId,
    nick: String,
    public: Boolean,
    info: Map[String, Any],
    following: Set[UserId],
    followers: Set[UserId],
    blocked: Set[UserId],
    topics: Set[TopicId],
    tags: Set[String])

/*
class UserM(val dbo: MongoDBObject) extends AccessModel {
  type ModelField[+A] = UserM.UserField[A]
}

object UserM {
  case class UserField[A : TypeTag](name: String) extends TypedField[A]

  val UID = UserField[UserId]("uid")
  val NICK = UserField[String]("nick")
  val PUBLIC = UserField[Boolean]("public")
  val INFO = UserField[Map]("info")
  val FOLLOWING = UserField[Set[String]]("following")
  val FOLLOWERS = UserField[Set[String]]("followers")
  val BLOCKED = UserField[Set[String]]("blocked")
  val TOPICS = UserField[Set[String]]("topics")
  val TAGS = UserField[Set[String]]("tags")

  /*implicit def UserMCodecJ: CodecJson[UserM] =
    CodecJson.casecodec9(UserM.apply, UserM.unapply)(UID.name, NICK.name, PUBLIC.name, INFO.name, FOLLOWING.name, FOLLOWERS.name, BLOCKED.name, TOPICS.name, TAGS.name)
*/
  lazy val allFields =
    (  UID
    :: NICK
    :: PUBLIC
    :: INFO
    :: FOLLOWING
    :: FOLLOWERS
    :: BLOCKED
    :: TOPICS
    :: TAGS
    :: Nil
    ) map { _.name }

  lazy val followerFields =
    (  UID
    :: NICK
    :: PUBLIC
    :: INFO
    :: FOLLOWING
    :: FOLLOWERS
    :: Nil
    ) map { _.name }

  lazy val publicFields =
    (  UID
    :: NICK
    :: PUBLIC
    :: Nil
    ) map { _.name }

  lazy val callerOnlyFields = allFields diff followerFields

  lazy val nonPublicFields = allFields diff publicFields

}


*/