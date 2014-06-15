package chatless


package object ops {
  import scalaz._
  type OperationResult[+A] = OperationFailure \/ A


  import scala.language.higherKinds
  @inline
  final def liftMOR[MT[_[+_], _], A](or: => OperationResult[A])(implicit mt: MonadTrans[MT]) =
    mt.liftM[OperationResult, A](or)

  import scalaz.std.{option => optionOps}

  @inline
  final def folder[X, Y](some: X => Y, none: => Y): Option[X] => Y = o => optionOps.cata(o)(some, none)

  implicit class OptionTOps[M[+_], A](opt: OptionT[M, A])(implicit M: Monad[M]) {

    def combine[B](ifSome: A => B, ifNone: => M[B]): M[B] = M.bind(opt.run) { folder(a => M.point(ifSome(a)), ifNone) }

    def alternatively(ifNone: => M[A]): M[A] = combine(identity, ifNone)
  }
}
