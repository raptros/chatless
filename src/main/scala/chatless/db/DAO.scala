package chatless.db
import chatless._
import chatless.model._

trait DAO {
  type ID
  type Model

  def get(id: ID): Option[Model]
}

trait UserDAO extends DAO {
  type ID = UserId
  type Model = User
  def get(id: UserId): Option[User]
}

trait TopicDAO extends DAO {
  type ID = TopicId
  type Model = Topic
}
