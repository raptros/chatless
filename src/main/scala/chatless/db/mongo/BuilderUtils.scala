package chatless.db.mongo

import com.mongodb.casbah.Imports._
import com.osinka.subset._
import com.mongodb.BasicDBObjectBuilder

object BuilderUtils {

  import scala.language.implicitConversions

  case class DBOKV[A](k: String, v: A)(implicit writer: BsonWritable[A]) {
    def write(builder: BasicDBObjectBuilder): BasicDBObjectBuilder = (writer(v) fold builder) { builder.add(k, _) }

    def write(buffer: DBObjectBuffer): DBObjectBuffer = buffer.append(k, v)(writer)
  }

  /** allows subset's DBO to work with Fields*/
  implicit class FieldAsDBOKey(f: Fields.Field) {
    def ->[A: BsonWritable](a: A) = DBOKV(f.toString, a)
  }

  object DBO2 {
    def empty: DBObjectBuffer = new DBObjectBuffer(BasicDBObjectBuilder.start)

    def apply(tuples: DBOKV[_]*): DBObjectBuffer = {
      val buffer =(tuples foldRight BasicDBObjectBuilder.start) { _ write _ }
      new DBObjectBuffer(buffer)
    }
  }

  /** allows eg buf.append(Field.banner -> "woah therE", Field.pos -> 333) */
  implicit class FieldsDBOBuffer(buf: DBObjectBuffer) {
    def attach(kvs: DBOKV[_]*): DBObjectBuffer = (kvs foldRight buf) { _ write _ }
  }
}
