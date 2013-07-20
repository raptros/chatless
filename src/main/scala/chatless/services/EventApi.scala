package chatless.services

import chatless._
import chatless.db._

import spray.routing._
import HListDeserializer._

import spray.http._
import spray.httpx.unmarshalling.Deserializer._

import argonaut._
import Argonaut._
import argonaut.DecodeJson._
import argonaut.EncodeJson._

import shapeless._
import shapeless.::
import chatless.operation.{OpSpec, ResEvents, OpRes}

trait EventApi extends ServiceBase with SpecDirectives {
  val events:Directive[UserId :: OpRes :: HNil] = (userAuth & pathPrefix("event")) map { cid:UserId =>
    cid :: ResEvents.asInstanceOf[OpRes] :: HNil
  }

  val getEvent:Directive1[OpSpec] = Relative.default(false) |
    Relative.last |
    Relative.at |
    Relative.before |
    Relative.from |
    Relative.after

  val eventApi:DOperation = (get & events & getEvent) as { operation }

}
