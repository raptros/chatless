package chatless.db

import com.mongodb.casbah.Imports._

trait DAO {
  type ID
  type Model

  def get(id: ID): Option[Model]

  def setOneField[ID2: AsQueryParam, A: Manifest : AsQueryParam](id: ID2, field: String, newVal: A): WriteStat
  def addToSet[ID2: AsQueryParam, A : Manifest : AsQueryParam](id: ID2, field: String, newItem: A): WriteStat
  def removeFromSet[ID2: AsQueryParam, A : Manifest : AsQueryParam](id: ID2, field: String, oldItem: A): WriteStat

}




