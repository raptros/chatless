package chatless.wiring.actors

import com.google.inject.{Key, TypeLiteral, Injector}
import akka.actor.{Props, Actor, IndirectActorProducer}
import chatless.services.ClientApiActor
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import chatless.db.OperationHandlerActor

class ActorInjector(val injector: Injector, val actorClass: Class[_ <: Actor]) extends IndirectActorProducer {
  def produce() = injector.getInstance(actorClass)
}

object ActorInjector {


  trait Provider {
    val injector: Injector
    def props[A <: Actor](implicit ct: ClassTag[A]) = Props(
      classOf[ActorInjector],
      injector,
      ct.runtimeClass.asSubclass(classOf[Actor]))
  }


  def providerFor(i: Injector) = new Provider {
    val injector = i
  }

}
