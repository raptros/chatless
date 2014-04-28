package chatless.db.mongo

import com.google.inject.Inject
import chatless.wiring.params.{ServerIdParam, TopicCollection}
import com.mongodb.casbah.Imports._
import chatless.model._
import scalaz._
import scalaz.syntax.std.option._
import scalaz.syntax.validation._
import scalaz.syntax.id._

import com.osinka.subset._

import argonaut._
import Argonaut._
import com.mongodb.casbah.commons.NotNothing
import chatless.db._
import com.mongodb.DuplicateKeyException
import chatless.db.NoSuchTopic
import chatless.db.DeserializationErrors

class MongoTopicDAO @Inject() (
    @ServerIdParam serverId: ServerCoordinate,
    @TopicCollection collection: MongoCollection,
    idGenerator: IdGenerator)
  extends TopicDAO {


  private def topicCoordAsQuery(coord: TopicCoordinate) = MongoDBObject(
    serverField -> coord.server,
    userField -> coord.user,
    idField -> coord.topic
  )


  def getTopic(coordinate: TopicCoordinate): DbError \/ Topic = for {
    dbo <- collection.findOne(topicCoordAsQuery(coordinate)) \/> NoSuchTopic(coordinate)
    topic <- parseTopic(dbo).disjunction leftMap { nel => DeserializationErrors(nel.list) }
  } yield topic

  private def parseTopic(dbo: DBObject): ValidationNel[String, Topic] = {
    val info = dbo.extractKey[String](infoField) flatMap { Parse.parse } leftMap { msg => s"could not parse info: $msg" }
    ApV.apply5(
      dbo.validateKeyNel[String](serverField),
      dbo.validateKeyNel[String](userField),
      dbo.validateKeyNel[String](idField),
      dbo.validateKeyNel[String](bannerField),
      info.validation.toValidationNel)(Topic.apply)
  }

  def listUserTopics(coordinate: UserCoordinate): Seq[TopicCoordinate] = {
    val q = MongoDBObject(serverField -> coordinate.server, userField -> coordinate.user)
    collection.find(q, MongoDBObject()).toSeq flatMap { _.getAs[String](idField) } map { coordinate.topic }
  }

  def createLocalTopic(user: String, init: TopicInit): DbError \/ String = init.fixedId map { id =>
    //in this case, an id has been specified; retry cannot be attempted if the id already exists.
    insertNewTopic(serverId.user(user).topic(id), init.banner, init.info.asJson.nospaces)
  } getOrElse {
    doInsertWithRetries(user, init)
  }

  private def doInsertWithRetries(user: String, init: TopicInit): DbError \/ String = {
    val userCoord = serverId.user(user)
    val json = init.info.asJson.nospaces
    def retry(tries: Int, tried: List[String]): DbError \/ String =
      if (tries <= 0) GenerateIdFailed(userCoord, tried).left else {
        val id = idGenerator.nextTopicId()
        val res = insertNewTopic(userCoord.topic(id), init.banner, json)
        res whenLeft { _.isInstanceOf[IdAlreadyUsed] } thenTry retry(tries - 1, id :: tried)
      }
    retry(3, Nil)
  }


  private def insertNewTopic(coord: TopicCoordinate, banner: String, info: String): DbError \/ String = {
    val dbo = MongoDBObject(idField -> coord.topic, serverField -> coord.server, userField -> coord.user, bannerField -> banner, infoField -> info)
    try {
      collection.insert(dbo)
      coord.topic.right
    } catch {
      case dup: DuplicateKeyException => IdAlreadyUsed(coord).left
      case t: Throwable => WriteFailure(t).left
    }
  }




  private def setup() {
    //todo: indexes
  }

  setup()
}

object MongoTopicDAO {

}
