package chatless.db.mongo

package object builders {
  import chatless.model.Coordinate
  import com.mongodb.casbah.Imports._
  import com.osinka.subset._

  import com.mongodb.casbah.commons.conversions.scala._
  RegisterJodaTimeConversionHelpers()

  /** allows subset's DBO to work with Fields*/
  implicit class FieldAsDBOKey(f: Fields.Value) {
    def -->[A: BsonWritable](a: A): DBOKV[A] = DBOKV(f.toString, a)
  }

  /** allows eg buf.append(Field.banner -> "woah therE", Field.pos -> 333) */
  implicit class FieldsDBOBuffer(buf: DBObjectBuffer) {
    def attach(kvs: DBOKV[_]*): DBObjectBuffer = (kvs foldRight buf) { _ write _ }
  }

  implicit class WritesToDBOWrapper[-A](a: A)(implicit w: WritesToDBO[A]) {
    def getBuffer: DBObjectBuffer = w.write(a)
    def getDBO: DBObject = getBuffer()
  }

  implicit class CoordinateAsQuery(c: Coordinate) {
    def asQuery = Builders.buildDBOForCoordinate(c.parent).attach(Fields.id --> c.idPart).apply()
  }

  implicit val coordinateWritable = Builders.coordinateWritesToDBO
  implicit val messageWritable = Builders.messageWritesToDBO
  implicit val topicWritable = Builders.topicWritesToDBO
}
