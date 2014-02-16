package chatless.services.routeutils

import spray.routing._
import shapeless._
import chatless.db.WriteStat
import scalaz.std.function._
import scalaz.syntax.bind._
import scalaz.syntax.monad._
import akka.event.LoggingAdapter
import spray.util.LoggingContext
import spray.httpx.marshalling.{ToResponseMarshaller, ToResponseMarshallable}

/** A CarryBuilder allows the construction of a CompleterCarrier associated with a specific path component.
  * @tparam L1 the types produced by the extractions
  * @param path the path component that will be paired with the carrier
  * @param extractions the extractions to use in the carrier
  */
class CarryBuilder[L1 <: HList](path: String, extractions: Directive[L1]) { self =>

  /** builds a CompleterCarrier and associates it with the path element.
    * @param f a function that accepts the extracted values and produces a route for them.
    * @return a path element paired with a completer carrier
    */
  def hBuild(f: L1 => Route): (String, CompleterCarrier) = path -> new CompleterCarrier {
    type L = L1
    val extractions = self.extractions
    val completer = f
  }
}

/** Contains implicit classes that provide useful methods for producing CompleterCarriers from CarryBuilders */
object CarryBuilder {

  /** the base HacBuilder implements a set of utility methods and defines the method needed to make them work */
  abstract class HacBuilder {
    import scala.language.higherKinds

    type In[A]

    type Out = (String, CompleterCarrier)

    type L <: HList

    /** the CarryBuilder that an implementation of HacBuilder will wrap */
    val cb: CarryBuilder[L]

    /** translates an A-supplying function into one that accepts cb's extractions */
    def apF[A](f: In[A])(l: L): A

    /** applies the CarryBuilder to a directly translated f */
    def build(f: In[Route]): Out = cb hBuild { apF(f) }

    /** wraps a function that performs a database operation in a carrier
      * @param f a function that produces the result of a database operation
      * @param lc implicit logging context for HelperDirectives.completeOp
      * @return a carrier that produces a route which supplies the extractions to f and produces a response that
      *         describes the result
      */
    def buildOp(f: In[WriteStat])(implicit lc: LoggingContext): Out = cb hBuild {
      { w: WriteStat => HelperDirectives.completeOp(w) }.compose(apF(f))
    }

    /** wraps a function that produces a result that can be sent back over the wire in a carrier
      * @tparam Q the result of f; it must be capable of being turned into a response
      * @param f a function that produces something that can be turned into a response
      * @param lc the implicit logging context to be passed through to Directives.complete
      * @return a carrier that produces a route which supplies the the extractions to f and
      *         uses the result as the response
      */
    def buildQuery[Q: ToResponseMarshaller](f: In[Q])(implicit lc: LoggingContext): Out = cb hBuild {
      { q: Q => Directives.complete { q } }.compose(apF(f))
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

