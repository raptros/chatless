package chatless.db

import chatless._

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import com.google.inject.Inject
import chatless.wiring.params.UserCollection
import chatless.model.{JDoc, User}

import scalaz._
import scalaz.syntax.id._

class SalatUserDAO @Inject() (
    @UserCollection collection: MongoCollection)
  extends SalatDAO[User, String](collection)
  with DAOHelpers
  with UserDAO {

  def get(id: UserId) = findOneById(id)


}
