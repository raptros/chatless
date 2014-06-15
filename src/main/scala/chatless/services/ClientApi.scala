package chatless.services
import spray.routing._
import chatless.db._
import spray.http._
import chatless.model._
import chatless.model.ids._
import spray.httpx.marshalling.{ToResponseMarshallable, Marshaller}
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
    topicDao.get(tc).fold(err => complete(err), provide)

  def withUser(uc: UserCoordinate): Directive1[User] =
    userDao.get(uc).fold(err => complete(err), provide)

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
    } ~ pathPrefix("message") {
      pathEndOrSingleSlash {
        get { //don't return all messages
          complete ("") //todo
        } ~ post {
          complete ("") //todo
        }
      }
    }
  }

  def pathSlashable[L <: HList](pm: PathMatcher[L]): Directive[L] = pathPrefix(pm ~ Slash.? ~ PathEnd)

  def meRoute(caller: User): Route =
    pathEndOrSingleSlash {
      get { complete(caller) }
    } ~ pathPrefix("about") {
      dynamic { //dangit
        localTopicRoute(caller, caller.coordinate.topic(caller.about))
      }
    } ~ pathPrefix("invites") {
      dynamic {
        localTopicRoute(caller, caller.coordinate.topic(caller.invites))
      }
    } ~ pathSlashable("topic") {
      get {
        complete { topicDao.listUserTopics(caller.coordinate) }
      } ~ (post & entity(as[TopicInit])) { ti =>
        complete { topicOps.createTopic(caller, ti) }
      }
    } ~ pathPrefix(topicIdMatcher) { tid =>
        localTopicRoute(caller, caller.coordinate.topic(tid))
    } ~ pathPrefix("pull") {
        complete { "no" } //todo
    }

  def loadLocalUser(userId: String) = userDao.get(serverId.user(UserId(userId))) getOrElse {
    throw new IllegalRequestException(StatusCodes.NotFound, s"could not find local user with id $userId")
  }

  def localUserRoute(caller: User, uc: UserCoordinate) = withUser(uc) { user =>
    pathEndOrSingleSlash {
      get { complete { user } }
    } ~  pathPrefix("about") {
      dynamic {
        localTopicRoute(caller, uc.topic(user.about))
      }
    } ~ pathSlashable("topic") {
      get { complete { topicDao.listUserTopics(uc) } }
    } ~ pathPrefix(topicIdMatcher) { tid =>
      localTopicRoute(caller, uc.topic(tid))
    }
  }

  def authedApi(callerId: String @@ UserId): Route = {
    val caller = userDao.get(serverId.user(callerId)) getOrElse {
      throw new RequestProcessingException(StatusCodes.InternalServerError)
    }
    pathPrefix("me") {
      meRoute(caller)
    } ~ pathPrefix(userWithoutServerMatcher) { user =>
      localUserRoute(caller, user)
    } ~ pathPrefix(userWithServerMatcher) { user =>
      complete {"no"} //todo
    }
  }
}
