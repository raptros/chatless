package chatless.wiring


import chatless.wiring.params._
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import akka.actor.{ActorRefFactory, ActorSelection, ActorSystem}
import scala.concurrent.duration._
import scalaz.syntax.id._

class SystemModule(val system: ActorSystem) extends DbModule {

  override def configure() {
    super.configure()
    
    val timeout = 5.seconds

    bind[ExecutionContext] toInstance system.dispatcher
    bind[Timeout] toInstance timeout

    bind[ActorRefFactory] toInstance system

    bind[ActorSelection].annotatedWith[LocalEventReceiverSelection] toInstance
      system.actorSelection(system / ActorNames.LOCAL_EVENT_RECV)

  }
}
