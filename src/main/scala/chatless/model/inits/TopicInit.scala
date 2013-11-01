package chatless.model.inits

import chatless._
import chatless.model.JDoc

case class TopicInit(
  title: String,
  public: Boolean = false,
  muted: Boolean = false,
  info: JDoc = JDoc(),
//todo make invites have more stuff associated so stuff can be set for each user
  invite: Set[UserId] = Set.empty[UserId],
  tags: Set[String] = Set.empty[String])
