package chatless.model

object MessageFields extends Enumeration {
  type MessageField = Value
  val x: Symbol = 'server
  val server, topic, user, id, timestamp, poster, joined, body, banner = Value
}
