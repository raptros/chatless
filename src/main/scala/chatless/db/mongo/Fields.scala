package chatless.db.mongo

object Fields extends Enumeration {

  type Field = Value
  //note: we basically never use _id in documents. this is intentional
  val _id, id, server, user, timestamp, message, topic, body, joined, poster, pos, banner, info, counter = Value
}


