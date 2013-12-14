package chatless.wiring

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import com.mongodb.casbah.Imports._
import chatless.db._
import chatless.wiring.params._

class DbModule extends AbstractModule with ScalaModule {
  def configure() {
    val mc = MongoClient()
    val chatlessDb = mc("chatless")
    val userCollection = chatlessDb("users")
    val topicCollection = chatlessDb("topics")
    val eventCollection = chatlessDb("events")
    val sequenceCollection = chatlessDb("sequences")

    bind[MongoClient] toInstance mc
    bind[MongoDB].annotatedWith[ChatlessDb] toInstance chatlessDb
    bind[MongoCollection].annotatedWith[UserCollection] toInstance userCollection
    bind[MongoCollection].annotatedWith[TopicCollection] toInstance topicCollection
    bind[MongoCollection].annotatedWith[EventCollection] toInstance eventCollection
    bind[MongoCollection].annotatedWith[SequenceCollection] toInstance sequenceCollection

    bind[UserDAO].to[SalatUserDAO].asEagerSingleton()
    bind[TopicDAO].to[SalatTopicDAO].asEagerSingleton()
    bind[EventDAO].to[SalatEventDAO].asEagerSingleton()
    bind[CounterDAO].to[SalatCounterDAO].asEagerSingleton()
  }
}
