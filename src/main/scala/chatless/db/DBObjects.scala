package chatless.db

import chatless._
import shapeless._
import argonaut._
import Argonaut._

import com.mongodb.casbah.Imports._

trait DBObjects {
  /** database object field extractor */
  type DBOFE = MongoDBObject => Option[Json]

  val fields:List[BaseDBOFE[_]]
  val fmap:Map[String, DBOFE] = (fields map { fe => fe.field -> fe }).toMap
  val fieldProj:Map[String, Int] = fmap.mapValues { _ => 1 }

  trait DBObjectOps {
    val dbo:MongoDBObject
    lazy val doit = (fe:DBOFE) => fe(dbo)
    def getJson(fname:String):Option[Json] = (fmap get fname) flatMap doit
  }

  abstract class BaseDBOFE[A:EncodeJson:Manifest](val field:String) extends DBOFE {
    def apply(dbo:MongoDBObject):Option[Json] = dbo.getAs[A](field) map { _.asJson }
  }
}
