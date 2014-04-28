package chatless.db

import chatless._
import chatless.model.User
import com.mongodb.casbah.Imports._
import scalaz.\/

trait UserDAO {

  def get(id: UserId): Option[User]


}
