package chatless.services

import spray.routing._

import shapeless._

import chatless._
import chatless.operation._


trait TopicApi extends ServiceBase {
  val topicApi = complete("yo")
}
