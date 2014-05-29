package chatless.db

import chatless._
import chatless.model.{UserCoordinate, User}
import com.mongodb.casbah.Imports._
import scalaz.\/

trait UserDAO {

  def get(user: UserCoordinate): DbError \/ User

  def insertUnique(user: User): DbError \/ String

}
