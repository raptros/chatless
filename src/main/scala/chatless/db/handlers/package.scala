package chatless.db

package object handlers {

  import chatless._
  import chatless.op2._

  trait UserOpHandler extends ((UserId, UserId, Specifier with ForUsers) => Any)

  trait TopicOpHandler extends ((UserId, TopicId, Specifier with ForTopics) => Any)

  trait MessagesOpHandler extends ((UserId, TopicId, Specifier with ForMessages) => Any)

  trait EventOpHandler extends ((UserId, Specifier with ForTopics) => Any)

}
