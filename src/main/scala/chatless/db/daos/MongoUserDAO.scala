package chatless.db.daos

import chatless._
import chatless.models.UserM
import com.mongodb.casbah.Imports._

import scalaz._
import scalaz.syntax.validation._
import scalaz.syntax.std.option._
import scalaz.syntax.applicative._
import scalaz.std.list._

import argonaut._
import Argonaut._

import com.google.inject.Inject
import com.google.inject.name.Named
import chatless.wiring.params.UserCollection
import chatless.db.{ModelExtractionError, UserNotFoundError}

class MongoUserDAO @Inject() (@UserCollection val collection: MongoCollection) extends UserDAO {

  def extract(dbo: MongoDBObject): ValidationNel[String, UserM] =
    (   (dbo extract UserM.UID) // map { _.asInstanceOf[UserId] }
    |@| (dbo extract UserM.NICK)
    |@| (dbo extract UserM.PUBLIC)
    |@| (dbo extract UserM.INFO)
    |@| (dbo extract UserM.FOLLOWING)
    |@| (dbo extract UserM.FOLLOWERS)
    |@| (dbo extract UserM.BLOCKED)
    |@| (dbo extract UserM.TOPICS)
    |@| (dbo extract UserM.TAGS)
    ) { UserM.apply }

  def get(uid: UserId) = for {
    userObj <- collection lookup MongoDBObject(UserM.UID.name -> uid) withError UserNotFoundError(uid)
    userM <- extract(userObj).disjunction leftMap { nel => ModelExtractionError("user", userObj, nel) }
  } yield userM

}
