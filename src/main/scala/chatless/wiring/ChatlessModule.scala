package chatless.wiring

import com.google.inject.{Provides, AbstractModule, Singleton}
import net.codingwell.scalaguice.ScalaModule
import com.google.inject.name.{Named, Names}
import com.mongodb.casbah.{MongoCollection, MongoDB, MongoClient}

import chatless.db.handlers._
import chatless.db.daos.{MongoTopicDAO, TopicDAO, UserDAO, MongoUserDAO}
import chatless.wiring.params._
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import chatless.db.{DatabaseActorClient, DatabaseAccessor}
import akka.actor.{ActorRefFactory, Props, ActorSelection, ActorSystem}
import chatless.services.ClientApiActor
import scala.concurrent.duration._

class ChatlessModule(val system: ActorSystem) extends AbstractModule with ScalaModule {

  def configure() {
    val mc = MongoClient()
    val chatlessDb = mc("chatless")
    val userCollection = chatlessDb("users")
    val topicCollection = chatlessDb("topics")

    val timeout = 5.seconds

    bind[ExecutionContext] toInstance system.dispatcher
    bind[Timeout] toInstance timeout

    bind[MongoClient] toInstance mc
    bind[MongoDB].annotatedWith[ChatlessDb] toInstance chatlessDb
    bind[MongoCollection].annotatedWith[UserCollection] toInstance userCollection
    bind[MongoCollection].annotatedWith[TopicCollection] toInstance topicCollection

    bind[UserDAO].to[MongoUserDAO].in[Singleton]
    bind[UserOpHandler].to[MongoUserHandler]

    bind[ActorRefFactory] toInstance system

    bind[TopicDAO].to[MongoTopicDAO].in[Singleton]
    bind[TopicOpHandler].to[MongoTopicHandler]

    val dbSel = system.actorSelection("/chatless-service-db")
    bind[ActorSelection].annotatedWith[DbActorSelection] toInstance dbSel

    bind[DatabaseAccessor].to[DatabaseActorClient].in[Singleton]


  }



}
