package chatless.models

abstract class ResponseWrapper[A : Manifest]{
  val v: A
}

case class StringR(v: String) extends ResponseWrapper[String]

case class BoolR(v: Boolean) extends ResponseWrapper[Boolean]
