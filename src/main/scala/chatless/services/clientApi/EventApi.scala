package chatless.services.clientApi

import spray.routing._

import chatless.services.{CallerRoute, ServiceBase, EVENT_API_BASE}
import chatless.db.{UserDAO, EventDAO}
import chatless.model.{User, Event}
import chatless.responses.UserNotFoundError
import chatless.ops.UserOps

trait EventApi extends ServiceBase {

  val eventDao: EventDAO
  val userOps: UserOps

  private val getCount:Directive1[Int] = (pathEnd & provide(1)) | path(IntNumber)

  private def completeWithQuery1(query: String)(operation: Int => Iterable[Event]): Route =
    (pathPrefix(query)  & getCount) { count: Int =>
      complete { operation(count) }
    }

  private def completeWithQuery2(query: String)(operation: (String, Int) => Iterable[Event]): Route =
    (pathPrefix(query / Segment) & getCount) { (id: String, count: Int) =>
      complete { operation(id, count) }
    }

  private def eventsFor(user: User) =
    pathEnd {
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
        path("oldest") {
          complete { Map("timestamp" -> eventDao.oldestKnownEventTime) }
        } ~ provide(userOps getOrThrow cid) {
          eventsFor
        }
      }
    }
  }
}
