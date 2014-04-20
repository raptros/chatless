package chatless.services

import spray.routing.authentication._

import spray.routing.HttpServiceActor
import scala.concurrent._
import chatless._
import com.google.inject.Inject
import chatless.db.{MessageDAO, TopicDAO, UserDAO}
import akka.actor.ActorLogging

/** this is the actor for the chatless service. */
class ClientApiActor @Inject() (
    val userDao: UserDAO,
    val topicDao: TopicDAO,
    val messageDao: MessageDAO)
  extends HttpServiceActor
  with ActorLogging
  with ClientApi {


  implicit val executionContext: ExecutionContext = actorRefFactory.dispatcher


  def getUserAuth: ContextAuthenticator[UserId] = BasicAuth("", _.user)

  val chatlessApi =path(PathEnd) {
    get {
      complete("yo")
    }
  }

  def receive = runRoute(chatlessApi)

}
