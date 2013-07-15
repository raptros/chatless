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



trait MeApi extends ServiceBase with SpecDirectives {
  val me:Directive[UserId :: OpRes :: HNil] = (userAuth & pathPrefix("me")) map { cid:UserId =>
    cid :: ResUser(cid).asInstanceOf[OpRes] :: HNil
  }
  private val gets:Directive1[OpSpec] = { path(PathEnd) & provide(GetAll) } |
    getPathField("nick") |
    getPathField("public") |
    getPathField("info") |
    getPathField("following") |
    testListPath("following") |
    getPathField("followers") |
    testListPath("followers") |
    getPathField("blocked") |
    testListPath("blocked") |
    getPathField("topics") |
    testListPath("topics") |
    getPathField("tags") |
    testListPath("tags")

  private val puts:Directive1[OpSpec] = replacePathField("nick") { StringVC.apply _ } |
    replacePathField("public") { BooleanVC.apply _ } |
    replacePathField("info") { JsonVC.apply _ } |
    addListPath("following") |
    addListPath("blocked") |
    addListPath("topics") |
    addListPath("tags")

  private val deletes:Directive1[OpSpec] = deleteListPath("following") |
    deleteListPath("followers") |
    deleteListPath("blocked") |
    deleteListPath("topics") |
    deleteListPath("tags")

  private val getting:Directive[UserId :: OpRes :: OpSpec :: HNil] = get & me & gets

  private val putting:Directive[UserId :: OpRes :: OpSpec :: HNil] = put & me & puts

  private val deleting:Directive[UserId :: OpRes :: OpSpec :: HNil] = delete & me & deletes

  val meApi:DOperation = (getting | putting | deleting) as { operation }
}
