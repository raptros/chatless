package chatless.db
import chatless._
import argonaut._
import Argonaut._
import scalaz._
import scalaz.Validation._
import scalaz.std.list._
import scalaz.std.string._
import scalaz.syntax.bind._
import scalaz.syntax.validation._
import chatless.operation.{ValueContainer, GetField, OpRes}

trait ModelAccess { self =>

  type FieldAccess[A] = A
  type FieldReplace[A] = A => self.type
  type UpdateListField[+A] = A => ListFieldUpdater[A, self.type]

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

  def mkUpdateListField[A:CodecJson](addF: A => self.type, removeF: A => self.type):UpdateListField[A] = { (a:A) =>
    new ListFieldUpdater[A, self.type] {
      def addTo() = addF(a)
      def removeFrom() = removeF(a)
    }
  }

  class UpdateWithAdd[A:CodecJson](val addF: A => self.type) {
    def withRemover(removeF: A => self.type):UpdateListField[A] = { (a:A) =>
      new ListFieldUpdater[A, self.type] {
        def addTo() = addF(a)
        def removeFrom() = removeF(a)
      }
    }
  }

  def makeAppender[A:CodecJson](addF: A => self.type):UpdateWithAdd[A] = new UpdateWithAdd[A](addF)


  def forCaller(cid:UserId):Accessor = new Accessor(cid)


  abstract class ListFieldUpdater[-A:CodecJson, +B] {
    def addTo():B
    def removeFrom():B
  }

  class Accessor(cid:UserId) {
    protected def getF(field:String):ValidationNel[StateError, (String, Json)] = getFieldFor(cid, field) map { field := _ }

    def getAll:StateValidJson = {
        val js = fields.keys flatMap { k => getF(k).toOption }
      (js foldLeft jEmptyObject) { jAddF }.successNel[StateError]
    }

    def getField(field:String):StateValidJson = getF(field) map { p => p ->: jEmptyObject }

//    def replaceField(field:String, value:ValueContainer):StateValidJson =
  }
}
