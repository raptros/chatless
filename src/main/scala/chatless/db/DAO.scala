package chatless.db

import com.mongodb.casbah.Imports._

trait DAO {
  type ID
  type Model

  def get(id: ID): Option[Model]

  def setOneField[A: Manifest : AsQueryParam](id: ID, field: String, newVal: A): WriteStat
  def addToSet[A : Manifest : AsQueryParam](id: ID, field: String, newItem: A): WriteStat
  def removeFromSet[A : Manifest : AsQueryParam](id: ID, field: String, oldItem: A): WriteStat

}




