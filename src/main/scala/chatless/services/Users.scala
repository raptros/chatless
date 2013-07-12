package chatless.services

import spray.routing._
import shapeless._
import chatless._
import chatless.db._
import shapeless.::

trait Users extends ServiceBase with SpecDirectives {
  private val usersBase:Directive1[UserId] = userAuth
  private val pPublic:Directive[UserId :: UserId :: HNil] = user & path("public" / PathEnd)

  val me:Directive[UserId :: UserId :: HNil] = (usersBase & pathPrefix("me")) map { uid =>
    uid :: uid :: HNil
  }

  val user:Directive[UserId :: UserId :: HNil] = me | (usersBase & pathPrefix(Segment))

  val getUser:DOperation = (get & user & path(PathEnd)) as {
    (cid:UserId, ruid:UserId) => Operation(cid, ResUser(ruid), GetAll)
  }

  val nick:DOperation = (user & stringField("nick")) as {
    (cid:UserId, ruid:UserId, spec:OpSpec) => Operation(cid, ResUser(ruid), spec)
  }

  val userPublic:DOperation = (user & booleanField("public")) as {
    (cid:UserId, ruid:UserId, spec:OpSpec) => Operation(cid, ResUser(ruid), spec)
  }

  val userApi:DOperation = getUser | nick | userPublic

}
