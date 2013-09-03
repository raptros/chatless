package chatless.services

import spray.routing.authentication._

import spray.routing.HttpServiceActor
import scala.concurrent._
import chatless._
import com.google.inject.Inject
import chatless.services.clientApi._
import scalaz.syntax.semigroup._
import chatless.db.{TopicDAO, UserDAO}
import scalaz.std.function._

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

  val chatlessApi = provide("id2") {
    callerRouteApi
  } ~ path(PathEnd) {
    get {
      complete("yo")
    }
  }

  def receive = runRoute(chatlessApi)

}
