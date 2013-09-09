package chatless.events.model

trait Deltas {
  val resource: Resource.Value

  trait MatcherBase {
    type A

    val action: Action.Value

    protected def matchField(f: Option[String]): Boolean
    protected def extract(d: Delta): Option[A]

    protected def start = Delta(res = resource, action = this.action)
    protected val test: Delta => Boolean = delta => delta.res == resource && delta.action == action && matchField(delta.field)

    def unapply(delta: Delta): Option[A] = if (test(delta)) extract(delta) else None
  }

  trait Matcher1 extends MatcherBase {
    type A1
    type A = A1
    protected def set(a1: A1)(delta: Delta): Delta

    def apply(a1: A1): Delta = set(a1) { start }
  }

  trait Matcher2 extends MatcherBase {
    type A1
    type A2
    type A = (A1, A2)

    protected def set(a1: A1, a2: A2)(delta: Delta): Delta
    def apply(a1: A1, a2: A2): Delta = set(a1, a2) { start }
  }
}
