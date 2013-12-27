package chatless.services.clientApi
import chatless._
import chatless.services.routeutils._

import chatless.services.{CallerRoute, ServiceBase, EVENT_API_BASE}
import chatless.db.EventDAO
import chatless.model.User
import chatless.ops.UserOps


trait EventApi extends ServiceBase {

  val eventDao: EventDAO
  val userOps: UserOps

  private def eventsFor(user: User) = pathEndOrSingleSlash {
    complete { eventDao.last(user, 1) }
  } ~ routeCarriers(
    "last"   carry getCount      buildQuery { eventDao.last(user, _) },
    "at"     carry getIdAndCount buildQuery { eventDao.at(user, _, _) },
    "from"   carry getIdAndCount buildQuery { eventDao.from(user, _, _) },
    "before" carry getIdAndCount buildQuery { eventDao.before(user, _, _) },
    "after"  carry getIdAndCount buildQuery { eventDao.after(user, _, _) }
  )

  val eventApi: CallerRoute = cid => pathPrefix(EVENT_API_BASE) {
    get {
      resJson {
        logResponse("event-get") {
          provide(userOps getOrThrow cid) {
            eventsFor
          } ~ fPath("oldest") {
            complete { Map("timestamp" -> eventDao.oldestKnownEventTime) }
          }
        }
      }
    }
  }
}
