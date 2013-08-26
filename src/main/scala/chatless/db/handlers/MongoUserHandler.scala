package chatless.db.handlers

import chatless._
import chatless.op2._
import argonaut._
import Argonaut._

import com.mongodb.casbah.Imports._
import chatless.db._
import chatless.op2.UpdateInfo
import chatless.op2.ReplaceNick
import chatless.op2.BlockUser
import chatless.op2.UnblockUser
import chatless.op2.RemoveFollower
import chatless.op2.JoinTopic
import chatless.op2.RemoveTag
import chatless.op2.LeaveTopic
import chatless.op2.FollowUser
import chatless.op2.SetPublic
import chatless.op2.AddTag
import chatless.op2.UnfollowUser
import chatless.db.daos.UserDAO
import com.google.inject.Inject

class MongoUserHandler @Inject() (val userDao: UserDAO)
  extends UserOpHandler {

  def apply(cid: UserId, uid: UserId, spec: Specifier with ForUsers) = spec match {
    case GetRes => userDao get uid valueOr { e => throw e }
    case ReplaceNick(nick) => ???
    case SetPublic(public) => ???
    case UpdateInfo(info) => ???
    case FollowUser(user, additional) => ???
    case UnfollowUser(user) => ???
    case RemoveFollower(user) => ???
    case BlockUser(user) => ???
    case UnblockUser(user) => ???
    case JoinTopic(topic, additional) => ???
    case LeaveTopic(topic) => ???
    case AddTag(tag) => ???
    case RemoveTag(tag) => ???
  }

}
