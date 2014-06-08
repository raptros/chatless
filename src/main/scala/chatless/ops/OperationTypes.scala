package chatless.ops

object OperationTypes extends Enumeration {
  type OperationType = Value
  val CREATE_TOPIC,
  SET_FIRST_MEMBER,
  SEND_INVITE,
  ADD_MEMBER,
  SET_MEMBER,
  GET_MEMBER,
  SEND_MESSAGE,
  LIST_MEMBERS,
  JOIN_TOPIC,
  READ_TOPIC = Value

}
