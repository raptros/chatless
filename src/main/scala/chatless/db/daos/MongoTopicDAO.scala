package chatless.db.daos

import chatless._
import chatless.models.TopicM
import com.mongodb.casbah.Imports._

import argonaut._
import Argonaut._

import scalaz._
import scalaz.std.option._
import scalaz.syntax.applicative._
import scalaz.syntax.std.option._
import scalaz.std.list._
import scalaz.syntax.validation._

import shapeless._
import com.google.inject.Inject
import chatless.wiring.params.TopicCollection
import chatless.db.{ModelExtractionError, TopicNotFoundError}


class MongoTopicDAO @Inject() (@TopicCollection val collection: MongoCollection) extends TopicDAO {

  def extract(dbo: MongoDBObject): ValidationNel[String, TopicM] =
    (   (dbo extract TopicM.TID) |@| (dbo extract TopicM.TITLE)
    |@| (dbo extract TopicM.PUBLIC)
    |@| (dbo extract TopicM.INFO)
    |@| (dbo extract TopicM.OP)
    |@| (dbo extract TopicM.SOPS)
    |@| (dbo extract TopicM.PARTICIPATING)
    |@| (dbo extract TopicM.TAGS)
    ) { TopicM.apply }

  def get(id: TopicId): ValidModel[TopicM] = for {
    obj <- collection lookup MongoDBObject(TopicM.TID.name -> id) withError TopicNotFoundError(id)
    model <- extract(obj).disjunction leftMap { nel => ModelExtractionError("topic", obj, nel) }
  } yield model
}
