package chatless.ops

import chatless.db.WriteStat
import chatless.model.Event

package object sequencers {
  import scalaz._
  import scalaz.std.list._
  import scalaz.syntax.std.boolean._
  import scalaz.syntax.id._

  type WriterTEither[W, L, R] = WriterT[({type e[+r] = L \/ r})#e, W, R]

  object WriterTEither extends WriterTInstances with WriterTFunctions {
    def apply[W, L, R](v: L \/ (W, R)) = WriterT[({type e[+r] = L \/ r})#e, W, R](v)
  }

  def stepEither[L, R](bPrev: Boolean, last: R)(next: => L \/ R): L \/ R =
    if (!bPrev) next else last.right

  def wrapEither[I, L, R](v: => L \/ R) = WriterTEither { v map { List.empty[I].-> } }

  def writeItem[I, L](i: I) = WriterTEither[List[I], L, Unit] { (List(i) -> ()).right[L] }

  def cWrite1[I, L](i: I)(c: Boolean) = WriterTEither {
    ((c ?? List(i)) -> c).right[L]
  }

  def cWrite2[I, L](c: Boolean)(i: I) = WriterTEither {
    ((c ?? List(i)) -> c).right[L]
  }

  implicit class WritableEither[L](e: L \/ Boolean) {
    def withEvent[I](event: I) = wrapEither[I, L, Boolean](e) flatMap {
      cWrite1 { event }
    }
  }

  implicit class BooleanEitherGuard(b: Boolean) {
    def step[L](v: => L \/ Boolean) = stepEither(b, b)(v)
  }
}

