package chatless.wiring

import com.google.inject.{Provides, AbstractModule, Singleton}
import net.codingwell.scalaguice.ScalaModule
import com.google.inject.name.{Named, Names}
import com.mongodb.casbah.{MongoCollection, MongoDB, MongoClient}

import chatless.wiring.params._
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import akka.actor.{ActorRefFactory, Props, ActorSelection, ActorSystem}
import scala.concurrent.duration._
import chatless.db._
import chatless.ops.{TopicOps, UserOps}
import chatless.ops.sequencers.{TopicOpSequences, UserOpSequences}
import com.google.inject.assistedinject.FactoryModuleBuilder

class ChatlessModule(val system: ActorSystem) extends AbstractModule with ScalaModule {

  def configure() {
    val mc = MongoClient()
    val chatlessDb = mc("chatless")
    val userCollection = chatlessDb("users")
    val topicCollection = chatlessDb("topics")
    val eventCollection = chatlessDb("events")

    val timeout = 5.seconds

    bind[ExecutionContext] toInstance system.dispatcher
    bind[Timeout] toInstance timeout

    bind[MongoClient] toInstance mc
    bind[MongoDB].annotatedWith[ChatlessDb] toInstance chatlessDb
    bind[MongoCollection].annotatedWith[UserCollection] toInstance userCollection
    bind[MongoCollection].annotatedWith[TopicCollection] toInstance topicCollection
    bind[MongoCollection].annotatedWith[EventCollection] toInstance eventCollection

    bind[ActorRefFactory] toInstance system

    bind[ActorSelection].annotatedWith[LocalEventReceiverSelection] toInstance system.actorSelection(system / ActorNames.LOCAL_EVENT_RECV)

    bind[UserDAO].to[SalatUserDAO].asEagerSingleton()
    bind[TopicDAO].to[SalatTopicDAO].asEagerSingleton()
    bind[EventDAO].to[SalatEventDAO].asEagerSingleton()

    install(FMB.bindImpl[UserOps, UserOpSequences].bindImpl[TopicOps, TopicOpSequences].buildF[OpsFactory])
  }



}
