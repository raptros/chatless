package chatless.services.clientApi

import chatless.services._

trait MessagesApi extends ServiceBase {


  val messagesApi: CallerRoute = cid => pathPrefix(TOPIC_API_BASE / Segment / MESSAGE_API_BASE) { tid: String =>
    complete { "no " }
  }
}
