package chatless.services.clientApi

import spray.routing._

import chatless.services.{CallerRoute, ServiceBase, EVENT_API_BASE}
import chatless.db.{UserDAO, EventDAO}
import chatless.model.{User, Event}
import chatless.responses.UserNotFoundError

trait EventApi extends ServiceBase {

  val eventDao: EventDAO
  val userDao: UserDAO

  private val getCount:Directive1[Int] = (path(PathEnd) & provide(1)) | path(IntNumber / PathEnd)

  private def completeWithQuery1(query: String)(operation: Int => Iterable[Event]): Route =
    (pathPrefix(query)  & getCount) { count: Int =>
      complete { operation(count) }
    }

  private def completeWithQuery2(query: String)(operation: (String, Int) => Iterable[Event]): Route =
    (pathPrefix(query / Segment) & getCount) { (id: String, count: Int) =>
      complete { operation(id, count) }
    }

  private def eventsFor(user: User) =
    path(PathEnd) {
      complete { eventDao.last(user) }
    } ~ completeWithQuery1("last") {
      eventDao.last(user, _)
    } ~ completeWithQuery2("at") {
      eventDao.at(user, _, _)
    } ~ completeWithQuery2("before") {
      eventDao.before(user, _, _)
    } ~ completeWithQuery2("from") {
      eventDao.from(user, _, _)
    } ~ completeWithQuery2("after") {
      eventDao.after(user, _, _)
    }

  val eventApi: CallerRoute = cid => pathPrefix(EVENT_API_BASE) {
    get {
      resJson {
        path("oldest" / PathEnd) {
          complete { Map("timestamp" -> eventDao.oldestKnownEventTime) }
        } ~ provide(userDao get cid getOrElse { throw UserNotFoundError(cid) }) {
          eventsFor _
        }
      }
    }
  }
}
