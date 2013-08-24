package chatless.db.daos

import chatless._
import chatless.models.UserM
import com.mongodb.casbah.Imports._

import scalaz._
import scalaz.syntax.validation._
import scalaz.syntax.applicative._
import scalaz.std.list._

import argonaut._
import Argonaut._

import com.google.inject.Inject
import com.google.inject.name.Named
import chatless.wiring.params.UserCollection

class MongoUserDAO @Inject() (@UserCollection val userCollection: MongoCollection) extends UserDAO {

  def extractUser(dbo: MongoDBObject): ValidationNel[String, UserM] = {
    val vUID = extractField[UserId](UserM.UID, dbo)
    val vNICK = extractField[String](UserM.UID, dbo)
    val vPUBLIC = extractField[Boolean](UserM.PUBLIC, dbo)
    val vINFO = extractField[String](UserM.INFO, dbo) flatMap { _.parse.validation.toValidationNel }
    val vFOLLOWING = extractField[Set[String]](UserM.FOLLOWING, dbo)
    val vFOLLOWERS = extractField[Set[String]](UserM.FOLLOWERS, dbo)
    val vBLOCKED = extractField[Set[String]](UserM.BLOCKED, dbo)
    val vTOPICS = extractField[Set[String]](UserM.TOPICS, dbo)
    val vTAGS = extractField[Set[String]](UserM.TAGS, dbo)

    (vUID |@| vNICK |@| vPUBLIC |@| vINFO |@| vFOLLOWING |@| vFOLLOWERS |@| vBLOCKED |@| vTOPICS |@| vTAGS) { UserM.apply }
  }

  def get(uid: UserId) = None /*for {
    userObj <- userCollection findOne MongoDBObject(UserM.UID -> uid)
  } yield*/

}
