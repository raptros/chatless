package chatless.services

object ErrorTypes extends Enumeration {
  type ErrorType = this.Value
  //Db error types
  val ID_ALREADY_USED,
  GENERATE_ID_FAILED,
  READ_FAILURE,
  WRITE_FAILURE,
  MISSING_COUNTER,
  DECODE_FAILURE = Value

  //operation failure types

}
