package chatless.services
import spray.routing._
import chatless.db._
import spray.http._
import chatless.model._
import chatless.model.ids._
import spray.httpx.marshalling.Marshaller
import argonaut._
import Argonaut._
import spray.httpx.unmarshalling.FromRequestUnmarshaller
import scalaz._
import chatless.model.topic.{MemberMode, Topic, TopicInit}
import shapeless._
import chatless.ops.Created
import chatless.ops.topic.TopicOps
import MarshallingImplicits._

trait ClientApi extends HttpService with IdMatchers {

  val serverId: ServerCoordinate
  val userDao: UserDAO
  val topicDao: TopicDAO
  val topicMemberDao: TopicMemberDAO
  val topicOps: TopicOps

  def withTopic(tc: TopicCoordinate): Directive1[Topic] =
    topicDao.get(tc) map {
      provide
    } valueOr { err => complete(err)}

  def withUser(uc: UserCoordinate): Directive1[User] =
    userDao.get(uc) map {
      provide
    } valueOr { err => complete(err)}

  def emptyJson: Directive1[Json] = requestEntityEmpty & provide(jEmptyObject)

  def targetMemberRoute(caller: User, topic: Topic, uc: UserCoordinate): Route =
    get {
      complete {
        topicOps.getMember(caller, topic, uc)
      }
    } ~ (put & entity(as[MemberMode])) { mode =>
      complete {
        topicOps.setMember(caller, topic, uc, mode)
      }
    } ~ (post & (entity(as[Json]) | emptyJson)) { j =>
      withUser(uc) { user =>
        complete {
          topicOps.inviteUser(caller, topic, user, j)
        }
      }
    }

  def localTopicRoute(caller: User, coordinate: TopicCoordinate): Route = withTopic(coordinate) { topic =>
    (pathEndOrSingleSlash & get) {
      complete { topic }
    } ~ pathPrefix("member") {
      (pathEndOrSingleSlash & get) {
        complete {
          topicOps.getMembers(caller, topic)
        }
      } ~ path(userCoordinateMatcher) {
        targetMemberRoute(caller, topic, _)
      }
    }
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
        } ~ (post & entity(as[TopicInit])) { ti =>
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
