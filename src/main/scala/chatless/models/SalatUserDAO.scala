package chatless.models
import chatless._

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import com.google.inject.Inject
import chatless.wiring.params.UserCollection

class SalatUserDAO @Inject() (
    @UserCollection collection: MongoCollection)
  extends SalatDAO[User, ObjectId](collection)
  with UserDAO {

  def get(uid: UserId): Option[User] = findOne(MongoDBObject("uid" -> uid))


  def getAsFields(uid: UserId, )
}
