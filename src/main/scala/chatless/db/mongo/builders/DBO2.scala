package chatless.db.mongo.builders

import com.osinka.subset._
import com.mongodb.BasicDBObjectBuilder

object DBO2 {
  def empty: DBObjectBuffer = new DBObjectBuffer(BasicDBObjectBuilder.start)

  def apply(tuples: DBOKV[_]*): DBObjectBuffer = new DBObjectBuffer(
    (tuples foldRight BasicDBObjectBuilder.start) {_ write _}
  )
}
