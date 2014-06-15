package chatless.services
import spray.routing._
import chatless.model._
import chatless.model.ids._
import spray.http._
import shapeless._
import scalaz.@@

trait IdMatchers extends PathMatchers with ImplicitPathMatcherConstruction  {
  val serverId: ServerCoordinate

  val serverMatcher: PathMatcher1[ServerCoordinate] = ("server" / Segment) hmap {
    case s :: HNil => ServerCoordinate(ServerId(s)) :: HNil
  }

  val serverIdMatcher: PathMatcher1[String @@ ServerId] = ("server" / Segment) hmap {
    case s :: HNil => ServerId(s) :: HNil
  }

  val userIdMatcher: PathMatcher1[String @@ UserId] = ("user" / Segment) hmap {
    case s :: HNil => UserId(s) :: HNil
  }

  val topicIdMatcher: PathMatcher1[String @@ TopicId] = ("topic" / Segment) hmap {
    case s :: HNil => TopicId(s) :: HNil
  }

  val userServerMatcher: PathMatcher[(String @@ ServerId) :: (String @@ UserId) :: HNil] = serverIdMatcher / userIdMatcher

  val userWithServerMatcher: PathMatcher1[UserCoordinate] = userServerMatcher hmap {
    case sid :: uid :: HNil => UserCoordinate(sid, uid) :: HNil
  }

  val userWithoutServerMatcher: PathMatcher1[UserCoordinate] = userIdMatcher hmap {
    case uid :: HNil => serverId.user(uid) :: HNil
  }

  val userCoordinateMatcher: PathMatcher1[UserCoordinate] = (userWithServerMatcher | userWithoutServerMatcher) ~ Slash.?

}
