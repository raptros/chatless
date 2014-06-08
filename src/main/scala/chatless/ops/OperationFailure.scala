package chatless.ops

import chatless.model._
import chatless.db.DbError
import chatless.model.topic.{MemberMode, TopicMode, TopicInit}
import scalaz.syntax.id._
import scalaz.std.{boolean => BoolUtils}
import OperationTypes.OperationType
import Preconditions.Precondition

sealed trait OperationFailure {
  def result[A]: OperationResult[A] = this.left[A]

  def failWhenM[A](b: Boolean): OperationResult[Unit] = BoolUtils.whenM[OperationResult, A](b)(result)

  def failUnlessM[A](b: Boolean): OperationResult[Unit] = BoolUtils.unlessM[OperationResult, A](b)(result)
}

case class PreconditionFailed(op: OperationType, failure: Precondition, coordinates: (String, Coordinate)*)
  extends OperationFailure

case class DbOperationFailed(op: OperationType, resource: Coordinate, cause: DbError)
  extends OperationFailure

case class InnerOperationFailed(op: OperationType, resource: Coordinate, cause: OperationFailure)
  extends OperationFailure

object OperationFailure {
  import scala.language.higherKinds
  import scalaz.MonadTrans

  implicit class BooleanFailureConditions(b: Boolean) {

    def failWhenM[A](f: => OperationFailure): OperationResult[Unit] =
      BoolUtils.whenM[OperationResult, A](b)(f.result)

    def failWhenLiftM[G[_[_], _]](f: => OperationFailure)(implicit G: MonadTrans[G]): G[OperationResult, Unit] =
      G.liftM[OperationResult, Unit](failWhenM(f))

    def failUnlessM[A](f: => OperationFailure): OperationResult[Unit] =
      BoolUtils.unlessM[OperationResult, A](b)(f.result)

    def failUnlessLiftM[G[_[_], _]](f: => OperationFailure)(implicit G: MonadTrans[G]): G[OperationResult, Unit] =
      G.liftM[OperationResult, Unit](failUnlessM(f))
  }

}
