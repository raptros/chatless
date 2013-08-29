package chatless

import scalaz._
import chatless.db.StateError

package object models {
  type ValidModel[A] = StateError \/ A

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
}
