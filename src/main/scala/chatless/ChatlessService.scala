package chatless

import spray.routing._
import spray.http._
import MediaTypes._

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import shapeless._

import scala.concurrent._
import ExecutionContext.Implicits.global

/*
paths

user:
(anywhere there is a uid. note that both "/me" and "" are shorthand for "/:<uid of logged in user>" )
[/:uid]/ -> user handle
[/:uid]/nick
[/:uid]/public ->  boolean (yes: other users can see my info etc, follows are autoapproved.
                             no: only current followers can see anything below, all requests require approval)
[/:uid]/info -> user info object
[/:uid]/following -> user list
[/:uid]/followers -> user list
[/:uid]/topics -> topic handle list (only ones the logged in user can see)
(below: only if uid == logged in user)
[/:uid]/blocked -> user list
[/:uid]/tags -> tags list

topics:
/topics/ -> the topics the user is participating in (see [/:uid]/topics)
(only if public or user is participating)
/topics/:tid -> topic object {id:<tid>, op:<uid>, title:<string>}
/topics/:tid/title -> string
(only if the logged in user is in the topic participants list)
/topics/:tid/public -> boolean
/topics/:tid/info -> object
/topics/:tid/tags -> tags list
/topics/:tid/participants -> user list

messages:
/topics/:id/messages -> messages queue
/topics/:id/messages/:id -> get a message object

events:
/events/ -> events queue (for currently logged-in user)
/events/:id/ -> get event at id

/requests/ -> request queue

/tag/:string -> topic list (any public topic with this tag

 */


/** defines the chatless service */
trait ChatlessService extends HttpService {
  val eventsBase:Directive0 = pathPrefix("events")

  /** /topics/ is the path to a user's topics participations list */


  val topics = path(PathEnd) {
    get {
      respondWithMediaType(`application/json`) {
        complete {
          val v = JObject("reason" -> JString("no user")) ~ ("results" -> List.empty[String])
          compact(render(v))
        }
      }
    }
  }

  val userAuth:Directive1[UserId] = provide("qqrrrrtz")

  val topicsBase:Directive1[UserId] = userAuth & pathPrefix("topics")
  val singleTopic:Directive[UserId :: TopicId :: HNil] = topicsBase & pathPrefix(Segment)
  val topicTitle:Directive[UserId :: TopicId :: HNil] = singleTopic & path("title" / PathEnd)
  val topicTitlePut:Directive[UserId :: TopicId :: String :: HNil] = put & topicTitle & anyParam('title.as[String])

  val getTopics:Directive1[JValue] = for {
    uid <- get & topicsBase
  } yield JArray(Nil).asInstanceOf[JValue]

  val getSingleTopic:Directive1[JValue] = for {
    p <- (get & singleTopic) as { Tuple2[UserId, TopicId] _ }
    (cid, rtid) = p
    res <- onComplete(retrieveTopicHandle(rtid, cid))
    resT <- res convSuccess { case TopicHandle(tid, opid, title) =>
      val r = pair2Assoc("tid" -> tid) ~ ("opid" -> opid) ~ ("title" -> title)
      provide(r)
    } convFailure { t =>
      provide(throwableToJson(t) ~ routeEnvToJson(Some(cid), Some(rtid)))
    }
  } yield resT.asInstanceOf[JValue]


  val getTopicTitle:Directive1[JValue] = for {
  //what would this actually have to do? well, test that the user can see the topic.
    p <- (get & topicTitle) as { Tuple2[UserId, TopicId] _ }
    (cid, rtid) = p
    res <- onComplete(retrieveTopicHandle(rtid, cid))
    resT <- res convSuccess {
      case TopicHandle(tid, opid, title) => provide(JString(title).asInstanceOf[JValue])
    } convFailure { t =>
      provide(throwableToJson(t) ~ routeEnvToJson(Some(cid), Some(rtid)))
    }
  } yield resT

  val putTopicTitle:Directive1[JValue] = for {
    t3 <- topicTitlePut as { Tuple3[UserId, TopicId, String] _ }
    (cid, rtid, newTitle) = t3
    res <- onComplete(setTopicTitle(rtid, cid, newTitle))
    resT <- res convSuccess { s =>
      provide(JString(s).asInstanceOf[JValue])
    } convFailure { t =>
      provide(throwableToJson(t) ~ routeEnvToJson(Some(cid), Some(rtid)))
    }
  } yield resT.asInstanceOf[JValue]

  val topicsApi = (getTopicTitle | putTopicTitle | getSingleTopic | getTopics) { jv =>
    complete {
      compact(render(jv))
    }
  }

  val chatlessApi = topicsApi ~ path(PathEnd) {
    get {
      complete("yo")
    }
  }

  def retrieveTopicHandle(tid:TopicId, cid:UserId):Future[TopicHandle] = future {
    TopicHandle("pants", "123z", "duck lord")
  }

  def setTopicTitle(tid:TopicId, cid:UserId, title:String):Future[String] = for {
    TopicHandle(topicId, opId, oldTitle) <- retrieveTopicHandle(tid, cid) if (cid == opId)
    res <- future { throw new NotImplementedError() }
  } yield "updated"

}
