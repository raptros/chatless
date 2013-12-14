package chatless.db

import chatless._

import com.novus.salat.dao._

import com.mongodb.casbah.Imports._
import chatless.model._
import chatless.wiring.params.SequenceCollection
import scalaz._
import scalaz.\/.fromTryCatch
import scalaz.std.option._
import scalaz.syntax.std.option._
import com.google.inject.Inject

class SalatCounterDAO @Inject()(
    @SequenceCollection collection: MongoCollection)
  extends SalatDAO[DbCounter, String](collection)
  with CounterDAO {

  def ensure(k: String) = runInner { ensureInner(k) } map { _ ? none[Long] | 0l.some }

  private def ensureInner(k: String) = collection.findAndModify(
    query = "_id" $eq k,
    fields = MongoDBObject(DbCounter.COUNTER -> 1),
    upsert = true,
    remove = false,
    update = $setOnInsert(DbCounter.COUNTER -> 0l),
    returnNew = false,
    sort = MongoDBObject()
  )

  def next(k: String) = ensure(k) flatMap { _ \/> "fake error!" ||| atomicInc(k) }

  private def atomicInc(k: String) = for {
    res <- runInner { atomicIncInner(k) }
    dbo <- res \/> s"something went wrong incremnting for $k; no object returned!"
    p   <- dbo.getAs[Long](DbCounter.COUNTER) \/>
      s"could not get value of ${DbCounter.COUNTER} for $k from $dbo as a long!"
  } yield p

  private def atomicIncInner(k: String) = collection.findAndModify(
    "_id" $eq k,
    fields = MongoDBObject(DbCounter.COUNTER -> 1),
    sort = MongoDBObject(),
    remove = false,
    update = $inc(DbCounter.COUNTER -> 1l),
    returnNew = true,
    upsert = false
  )

  @inline private def runInner(inner: => Option[DBObject]) = fromTryCatch { inner } leftMap { _.getMessage }
}
