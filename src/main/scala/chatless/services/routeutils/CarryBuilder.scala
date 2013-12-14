package chatless.services.routeutils

import spray.routing.{HListable, ApplyConverter, Directive, Route}
import shapeless._
import chatless.db.WriteStat
import scalaz.std.function._
import scalaz.syntax.bind._
import scalaz.syntax.monad._
import akka.event.LoggingAdapter

class CarryBuilder[L1 <: HList](path:String, extractions: Directive[L1]) { self =>
  def hBuild(f: L1 => Route): (String, CompleterCarrier) = path -> new CompleterCarrier {
    type L = L1
    val extractions = self.extractions
    val completer = f
  }

}

object CarryBuilder {
  abstract class HacBuilder {
    import scala.language.higherKinds
    type In[A]
    type Out = (String, CompleterCarrier)
    type L <: HList
    val cb: CarryBuilder[L]

    def apF[A](f: In[A])(l: L): A

    def build(f: In[Route]): Out = cb hBuild { apF(f) }

    def buildOp(f: In[WriteStat])(implicit log: LoggingAdapter): Out = cb hBuild {
      { w: WriteStat => HelperDirectives.completeOp(w) }.compose(apF(f))
    }
  }

  implicit class HB1[T1](val cb: CarryBuilder[T1 :: HNil]) extends HacBuilder {
    type In[A] = (T1) => A
    type L = T1 :: HNil
    def apF[A](f: In[A])(l: L) = l match { case t1 :: HNil => f(t1) }
  }

  implicit class HB2[T1, T2](val cb: CarryBuilder[T1 :: T2 :: HNil]) extends HacBuilder {
    type L = T1 :: T2 :: HNil
    type In[A] = (T1, T2) => A
    def apF[A](f: In[A])(l: L) = l match { case t1 :: t2 :: HNil => f(t1, t2) }
  }

  implicit class HB3[T1, T2, T3](val cb: CarryBuilder[T1 :: T2 :: T3 :: HNil]) extends HacBuilder {
    type L = T1 :: T2 :: T3 :: HNil
    type In[A] = (T1, T2, T3) => A
    def apF[A](f: In[A])(l: L) = l match { case t1 :: t2 :: t3 :: HNil => f(t1, t2, t3) }
  }
}

