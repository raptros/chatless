package chatless.services
import chatless._
import spray.routing._
import chatless.db.{DbError, TopicDAO, UserDAO}
import spray.http._
import spray.http.MediaTypes._
import chatless.model.{TopicCoordinate, User}
import spray.httpx.marshalling.Marshaller
import argonaut._
import Argonaut._
import spray.httpx.unmarshalling.FromRequestUnmarshaller
import scalaz.\/
import MarshallingImplicits._
import chatless.model.topic.TopicInit

trait ClientApi extends HttpService {

  val userDao: UserDAO
  val topicDao: TopicDAO

  def localTopicRoute(caller: User, coordinate: TopicCoordinate): Route = complete { "no" }

//  def newTopicRoute()


  private def postedEntity[A](um: FromRequestUnmarshaller[A]): Directive1[A] = post & entity(um)

  def meRoute(caller: User): Route =
    pathEndOrSingleSlash {
      get { complete(caller) }
    } ~ pathPrefix("about") {
      localTopicRoute(caller, caller.coordinate.topic(caller.about))
    } ~ pathPrefix("topic") {
      pathEndOrSingleSlash {
        get {
          complete {
            topicDao.listUserTopics(caller.coordinate).toList
          }
        } ~ postedEntity(as[TopicInit]) { (ti: TopicInit) =>
        //create new topic, return url
          val uri = Uri.apply("/me/topic/giarneingbe")
          complete {
            (StatusCodes.Created, HttpHeaders.Location(uri) :: Nil, Json("this" := "fake"))
          }
        }
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
    }
  }

}
