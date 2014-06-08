package chatless.ops

object Preconditions extends Enumeration {
  type Precondition = Value
  val READ_DENIED,
  SET_MEMBER_DENIED,
  WRITE_DENIED,
  USER_NOT_LOCAL = Value

}
