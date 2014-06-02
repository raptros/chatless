package chatless.ops

import chatless.model._
import chatless.db.DbError
import chatless.model.topic.TopicInit
import scalaz.syntax.id._

sealed trait OperationFailure {
  def result[A]: OperationResult[A] = this.left[A]
}

case class AddMemberFailed(topic: TopicCoordinate, user: UserCoordinate, cause: DbError)
  extends OperationFailure

case class SendInviteFailed(topic: TopicCoordinate, user: UserCoordinate, cause: OperationFailure)
  extends OperationFailure

case class CreateTopicFailed(user: UserCoordinate, init: TopicInit, cause: DbError)
  extends OperationFailure

case class SetFirstMemberFailed(topic: TopicCoordinate, cause: DbError)
  extends OperationFailure

case class UserNotLocal(user: UserCoordinate, server: ServerCoordinate)
  extends OperationFailure


case class GetMembershipFailed(topic: TopicCoordinate, user: UserCoordinate, cause: DbError)
  extends OperationFailure

case class UserReadDenied(topic: TopicCoordinate, user: UserCoordinate)
  extends OperationFailure