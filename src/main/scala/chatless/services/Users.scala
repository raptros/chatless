package chatless.services

import spray.routing._
import shapeless._
import chatless._
import chatless.db._
import shapeless.::

trait Users extends ServiceBase with SpecDirectives {
  private val usersBase:Directive1[UserId] = userAuth
  private val nick:Directive[UserId :: UserId :: HNil] = user & path("nick" / PathEnd)
  private val pPublic:Directive[UserId :: UserId :: HNil] = user & path("public" / PathEnd)

  val me:Directive[UserId :: UserId :: HNil] = (usersBase & pathPrefix("me")) map { uid =>
    uid :: uid :: HNil
  }

  val user:Directive[UserId :: UserId :: HNil] = me | (usersBase & pathPrefix(Segment))

  val getUser:DOperation = (get & user & path(PathEnd)) as { (cid:UserId, ruid:UserId) =>
    Operation(cid, ResUser(ruid), GetAll)
  }

  val getNick:DOperation = (get & nick) as { (cid:UserId, ruid:UserId) =>
    Operation(cid, ResUser(ruid), GetFields("nick"))
  }

  val putNick:DOperation = (put & nick & dEntity(as[String])) as { (cid:UserId, ruid:UserId, newNick:String) =>
    Operation(cid, ResUser(ruid), ReplaceField("nick", newNick))
  }

  val userPublic:DOperation = (user & booleanField("public")) as { (cid:UserId, ruid:UserId, spec:OpSpec) =>
    Operation(cid, ResUser(ruid), spec)
  }
/*
  val getUserPublic:DOperation = (get & pPublic) as { (cid:UserId, ruid:UserId) =>
    Operation(cid, ResUser(ruid), GetFields("nick"))
  }*/

  /*val putUserPublic:DOperation = (put & pPublic & dEntity(as[Boolean])) as { (cid:UserId, ruid:UserId, nPublic:Boolean) =>
    Operation(cid, ResUser(ruid), ReplaceField("public", nPublic))
  }*/

  val userApi = getUser |
    getNick |
    putNick |
    userPublic

}
