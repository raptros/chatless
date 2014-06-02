package chatless.services
import chatless._
import spray.routing._
import chatless.db._
import spray.http._
import spray.http.MediaTypes._
import chatless.model._
import chatless.model.ids._
import spray.httpx.marshalling.{ToResponseMarshaller, Marshaller}
import argonaut._
import Argonaut._
import spray.httpx.unmarshalling.FromRequestUnmarshaller
import scalaz._
import scalaz.EitherT._
import MarshallingImplicits._
import chatless.model.topic.{MemberMode, Topic, TopicInit}
import shapeless._
import shapeless.::
import chatless.ops.{TopicOps, Created}

trait ClientApi extends HttpService {

  val serverId: ServerCoordinate
  val userDao: UserDAO
  val topicDao: TopicDAO
  val topicMemberDao: TopicMemberDAO
  val topicOps: TopicOps

  def withTopic(tc: TopicCoordinate): Directive1[Topic] =
    topicDao.get(tc) map { provide } valueOr { err => complete(err) }

  private def stringSegOpt(s: String): PathMatcher1[Option[String]] = (s / Segment).?

  private val serverMatcher: PathMatcher1[ServerCoordinate] =  stringSegOpt("server") hmap {
    case s :: HNil => (s fold serverId) { s1 => ServerCoordinate(ServerId(s1)) } :: HNil
  }

  private val userMatcher: PathMatcher1[UserCoordinate] = (serverMatcher / "user" / Segment) hmap {
    case server :: uid :: HNil => server.user(UserId(uid)) :: HNil
  }

  def localTopicRoute(caller: User, coordinate: TopicCoordinate): Route = withTopic(coordinate) { topic =>
    pathEndOrSingleSlash {
      get {
        complete {
          topic
        }
      }
    } ~ path("member") {
      get {
        complete { topicOps.getMembers(caller, topic) }
      }
    }
  }

//  def newTopicRoute()

  private def postedEntity[A](um: FromRequestUnmarshaller[A]): Directive1[A] = post & entity(um)

  private val localTopicMatcher: PathMatcher1[TopicCoordinate] = ("user" / Segment / "topic" / Segment) hmap {
    case uid :: tid :: HNil => serverId.user(UserId(uid)).topic(TopicId(tid)) :: HNil
  }

  def meRoute(caller: User): Route =
    pathEndOrSingleSlash {
      get { complete(caller) }
    } ~ pathPrefix("about") {
      dynamic { //dangit
        localTopicRoute(caller, caller.coordinate.topic(caller.about))
      }
    } ~ pathPrefix("topic") {
      pathEndOrSingleSlash {
        get {
          complete { topicDao.listUserTopics(caller.coordinate) }
        } ~ postedEntity(as[TopicInit]) { ti: TopicInit =>
          complete { topicOps.createTopic(caller, ti) }
        }
      } ~ pathPrefix(Segment) { topicId =>
        localTopicRoute(caller, caller.coordinate.topic(TopicId(topicId)))
      } ~ pathPrefix("pull") {
        complete { "no" }
      }
    }

  def loadLocalUser(userId: String) = userDao.get(serverId.user(UserId(userId))) getOrElse {
    throw new IllegalRequestException(StatusCodes.NotFound, s"could not find local user with id $userId")
  }

  def localUserRoute(caller: User) =
    pathPrefix(Segment) map { loadLocalUser } apply { user: User =>
      pathEndOrSingleSlash {
        get { complete(user) }
      }
    }

  def authedApi(callerId: String @@ UserId): Route = {
    val caller = userDao.get(serverId.user(callerId)) getOrElse {
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
