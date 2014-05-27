package chatless.db


package object mongo {
  import chatless.model.Coordinate
  import com.mongodb.DBObject

  import io.github.raptros.bson._
  import Bson._

  import codecs.Codecs.CoordinateEncodeBson

  import scalaz.NonEmptyList

  val wrapDecodeErrors: NonEmptyList[DecodeError] => DbError = nel => DeserializationErrors {
    nel.list map { _.toString() }
  }

  implicit class CoordinateUtil(c: Coordinate) {
    def query: DBObject = c.parent.asBson +@+ ("id" :> c.id)
  }


}
