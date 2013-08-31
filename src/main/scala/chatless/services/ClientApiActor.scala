package chatless.services

import akka.actor.Actor
import spray.routing.authentication._

import com.mongodb.casbah.Imports._
import spray.routing.{HttpServiceActor, HttpService, RequestContext}
import scala.concurrent._
import chatless._
import akka.util.Timeout
import scala.concurrent.duration._
import com.google.inject.Inject
import chatless.services.clientApi._
import scalaz._
import scalaz.std.function._
import scalaz.syntax.semigroup._
import chatless.models.UserDAO
import chatless.models.TopicDAO

/** this is the actor for the chatless service. */
class ClientApiActor @Inject() (
    val userDao: UserDAO,
    val topicDao: TopicDAO)
  extends HttpServiceActor
  with AllApis {

  val callerRoutes: List[CallerRoute] =
    meApi ::
    messagesApi ::
    topicApi ::
    userApi ::
    eventApi ::
    taggedApi ::
    Nil

  implicit val executionContext: ExecutionContext = actorRefFactory.dispatcher

  def callerRouteApi: CallerRoute = callerRoutes reduce { _ |+| _ }

  def getUserAuth: ContextAuthenticator[UserId] = BasicAuth("", _.user)

  val chatlessApi = path(PathEnd) {
    get {
      complete("yo")
    }
  }

  def receive = runRoute(chatlessApi)

}
