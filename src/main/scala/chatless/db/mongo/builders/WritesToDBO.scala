package chatless.db.mongo.builders

import scala.annotation.implicitNotFound
import com.osinka.subset._

@implicitNotFound("could not find a WritesToDBO instance for ${A}")
trait WritesToDBO[-A] {
  def write(a: A): DBObjectBuffer
}

object WritesToDBO {
  def apply[A](f: A => DBObjectBuffer): WritesToDBO[A] = new WritesToDBO[A] {
    def write(a: A): DBObjectBuffer = f(a)
  }
}
