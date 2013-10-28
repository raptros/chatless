package chatless.model

/** a type class for things that have ValueContainer implementations. */
abstract class ContainableValue[A] {
  def contain(a: A): ValueContainer
}

object ContainableValue {
  implicit val containableString = new ContainableValue[String] {
    def contain(a: String) = StringVC(a)
  }

  implicit val containableBoolean = new ContainableValue[Boolean] {
    def contain(a: Boolean) = BooleanVC(a)
  }
}

