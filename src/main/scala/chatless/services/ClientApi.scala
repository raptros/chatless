package chatless.services
import chatless._
import spray.routing._
import chatless.db.{DbError, TopicDAO, UserDAO}
import spray.http._
import spray.http.MediaTypes._
import chatless.model._
import spray.httpx.marshalling.{ToResponseMarshaller, Marshaller}
import argonaut._
import Argonaut._
import spray.httpx.unmarshalling.FromRequestUnmarshaller
import scalaz.\/
import MarshallingImplicits._
import chatless.model.topic.TopicInit
import shapeless._
import shapeless.::

trait ClientApi extends HttpService {

  val serverId: ServerCoordinate
  val userDao: UserDAO
  val topicDao: TopicDAO

  def localTopicRoute(caller: User, coordinate: TopicCoordinate): Route = complete { "no" }

//  def newTopicRoute()

  private def postedEntity[A](um: FromRequestUnmarshaller[A]): Directive1[A] = post & entity(um)

  private val localTopicMatcher: PathMatcher1[TopicCoordinate] = ("user" / Segment / "topic" / Segment) hmap {
    case uid :: tid :: HNil => serverId.user(uid).topic(tid) :: HNil
  }

  def created[A: ToResponseMarshaller](uri: Uri, a: A) = (StatusCodes.Created, HttpHeaders.Location(uri) :: Nil, a)


  import Uri.Path
  private def createTopic(user: User): Route = postedEntity(as[TopicInit]) { ti: TopicInit =>
    complete {
      topicDao.createLocal(user.id, ti) map { topic =>
        created(Uri(path = Path / "me" / "topic" / topic.id), topic)
      }
    }
  }

  def meRoute(caller: User): Route =
    pathEndOrSingleSlash {
      get { complete(caller) }
    } ~ pathPrefix("about") {
      localTopicRoute(caller, caller.coordinate.topic(caller.about))
    } ~ pathPrefix("topic") {
      pathEndOrSingleSlash {
        get {
          complete(topicDao.listUserTopics(caller.coordinate).toList)
        } ~ createTopic(caller)
      } ~ pathPrefix(Segment) { topicId =>
        localTopicRoute(caller, caller.coordinate.topic(topicId))
      }
    }

  def loadLocalUser(userId: String) = userDao.get(userId) getOrElse {
    throw new IllegalRequestException(StatusCodes.NotFound, s"could not find local user with id $userId")
  }

  def localUserRoute(caller: User) =
    pathPrefix(Segment) map { loadLocalUser } apply { user: User =>
      pathEndOrSingleSlash {
        get { complete(user) }
      }
    }

  def authedApi(callerId: UserId): Route = {
    val caller = userDao.get(callerId) getOrElse {
      throw new RequestProcessingException(StatusCodes.InternalServerError)
    }
    pathPrefix("me") {
      meRoute(caller)
    } ~ pathPrefix("user") {
      localUserRoute(caller)
    } ~ pathPrefix("server") {
      complete {"no"}
    }
  }
}
