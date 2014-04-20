package chatless.db

import chatless._
import chatless.model.{JDoc, User}
import com.mongodb.casbah.Imports._
import scalaz.\/

trait UserDAO {

  def get(id: UserId): Option[User]


}
