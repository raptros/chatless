package chatless.db


package object mongo {
  import chatless.model.Coordinate
  import com.mongodb.DBObject

  import io.github.raptros.bson._
  import Bson._

  import codecs.Codecs.{CoordinateEncodeBson, TaggedStringEncodeBsonField}

  import scalaz.NonEmptyList

  def wrapDecodeErrors(what: String, coordinate: Coordinate): NonEmptyList[DecodeError] => DbError = nel =>
    DecodeFailure(what, coordinate, nel.list map { _.toString() })

  implicit class CoordinateUtil(c: Coordinate) {
    def query: DBObject = c.parent.asBson +@+ ("id" :> c.id)
  }


}
