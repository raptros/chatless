package chatless.services.clientApi

import chatless.models.{User, UserDAO}

trait UserMethods {
  val userDao: UserDAO

  protected def mapUser(user: User): Map[String, Any] = {
    import User._
    (UID -> user.uid) ::
      (NICK -> user.nick) ::
      (PUBLIC -> user.public) ::
      (INFO -> user.info) ::
      (FOLLOWING -> user.following) ::
      (FOLLOWERS -> user.followers) ::
      (BLOCKED -> user.blocked) ::
      (TOPICS -> user.topics) ::
      (TAGS -> user.tags) ::
      Nil
  }.toMap

}
