package chatless.db.mongo.builders

import com.osinka.subset._
import com.mongodb.BasicDBObjectBuilder

case class DBOKV[A](k: String, v: A)(implicit writer: BsonWritable[A]) {
  def write(builder: BasicDBObjectBuilder): BasicDBObjectBuilder = (writer(v) fold builder) { builder.add(k, _) }

  def write(buffer: DBObjectBuffer): DBObjectBuffer = buffer.append(k, v)(writer)
}


