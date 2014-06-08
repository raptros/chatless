package chatless


package object ops {
  import scalaz._
  type OperationResult[+A] = OperationFailure \/ A


  import scala.language.higherKinds
  @inline
  final def liftMOR[MT[_[+_], _], A](or: => OperationResult[A])(implicit mt: MonadTrans[MT]) =
    mt.liftM[OperationResult, A](or)

}
