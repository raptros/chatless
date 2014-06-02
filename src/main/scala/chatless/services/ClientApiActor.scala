package chatless.services

import spray.routing.authentication._

import spray.routing.HttpServiceActor
import scala.concurrent._
import chatless._
import com.google.inject.Inject
import chatless.db.{TopicMemberDAO, MessageDAO, TopicDAO, UserDAO}
import akka.actor.ActorLogging
import chatless.wiring.params.ServerIdParam
import chatless.model.ServerCoordinate
import chatless.ops.TopicOps

/** this is the actor for the chatless service. */
class ClientApiActor @Inject() (
    @ServerIdParam
    val serverId: ServerCoordinate,
    val topicOps: TopicOps,
    val userDao: UserDAO,
    val topicDao: TopicDAO,
    val messageDao: MessageDAO,
    val topicMemberDao: TopicMemberDAO)
  extends HttpServiceActor
  with ActorLogging
  with ClientApi {


  implicit val executionContext: ExecutionContext = actorRefFactory.dispatcher


  def getUserAuth: ContextAuthenticator[String] = BasicAuth("", _.user)

  val chatlessApi =path(PathEnd) {
    get {
      complete("yo")
    }
  }

  def receive = runRoute(chatlessApi)

}
