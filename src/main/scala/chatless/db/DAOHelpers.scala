package chatless.db

import chatless._

import scalaz._
import scalaz.syntax.id._
import scalaz.syntax.std.option._

import com.novus.salat.dao._
import com.mongodb.casbah.Imports._

trait DAOHelpers { dao: SalatDAO[_, _] =>

  def setOneField[ID: AsQueryParam, A: Manifest: AsQueryParam](id: ID, field: String, newVal: A): WriteStat = {
    val modifyResult = dao.collection.update(
      q = $and("_id" $eq id, field $ne newVal),
      o = $set(field -> newVal))
    Option(modifyResult.getError) <\/ (modifyResult.getN > 0)
  }

  def addToSet[ID: AsQueryParam, A: Manifest: AsQueryParam](id: ID, field: String, newItem: A): WriteStat = {
    val modifyResult = dao.collection.update(
      q = $and("_id" $eq id, field $ne newItem),
      o = $addToSet(field -> newItem))
    Option(modifyResult.getError) <\/ (modifyResult.getN > 0)
  }

  def removeFromSet[ID: AsQueryParam, A: Manifest: AsQueryParam](id: ID, field: String, oldItem: A): WriteStat = {
    val modifyResult = dao.collection.update(
      q = $and("_id" $eq id, field $eq oldItem),
      o = $pull(field -> oldItem))
    Option(modifyResult.getError) <\/ (modifyResult.getN > 0)
  }

}
