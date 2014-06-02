package chatless.services

trait UnifiedErrorReporter[-A] {
  def apply(err: A): UnifiedErrorReport
}

object UnifiedErrorReporter {
  def apply[A](f: A => UnifiedErrorReport): UnifiedErrorReporter[A] = new UnifiedErrorReporter[A] {
    def apply(err: A) = f(err)
  }
}

