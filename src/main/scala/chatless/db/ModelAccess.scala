package chatless.db
import chatless._
import argonaut._
import Argonaut._
import scalaz._
import scalaz.syntax.validation._
import chatless.operation.{GetField, OpRes}

trait ModelAccess { self =>

  type FieldAccess[A:CodecJson] = A
  type FieldReplace[A:CodecJson] = A => self.type
  type UpdateListField[+A:CodecJson] = A => ListFieldUpdater[A, self.type]

  abstract class ListFieldUpdater[-A:CodecJson, +B] {
    def addTo():B
    def removeFrom():B
  }

  def updateListField[A:CodecJson](addF: A => self.type, removeF: A => self.type):UpdateListField[A] = (a:A) => new ListFieldUpdater[A, self.type] {
    def addTo() = addF(a)
    def removeFrom() = removeF(a)
  }

  /** map from fields to accessors*/
  def fields:Map[String, FieldAccess[_]]

  /** the replaceable fields*/
  def replaceableFields:Map[String, FieldReplace[_]]

  /** the list fields */
  def updatableListFields:Map[String, UpdateListField[_]]

  def canRead(cid:UserId, field:String):Boolean

  def canWrite(cid:UserId, field:String):Boolean

  def resFor(cid:UserId):OpRes

  def allowReadAccessFor(cid:UserId, field:String)(yes: =>Json):StateValidJson = if (canRead(cid, field)) yes.successNel else {
    AccessNotPermitted(cid, resFor(cid), GetField(field)).failNel
  }

  def getFieldFor(cid:UserId, field:String):StateValidJson = (fields get field) map { a =>
    allowReadAccessFor(cid, field) { a.asJson }
  } getOrElse NonExistentField(field, resFor(cid)).failNel

  def forCaller(cid:UserId):Accessor = new Accessor(cid)

  class Accessor(cid:UserId) {
    def getF(field:String):ValidationNel[StateError, (String, Json)] = getFieldFor(cid, field) map { field := _ }

    def getAll:StateValidJson = {
      val js:List[(String, Json)] = for ( field <- fields.keys; p <- getF(field)) yield p
      val j:Json = (js foldLeft jEmptyObject) { jAddF }
      j.successNel[StateError]
    }

    def getField(field:String):StateValidJson = getF(field) map { jEmptyObject.->: }

  }
}
