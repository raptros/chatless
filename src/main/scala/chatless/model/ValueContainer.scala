package chatless.model

import chatless._

import com.novus.salat._
import com.novus.salat.annotations._

@Salat
sealed trait ValueContainer {
  def isEmpty: Boolean
  def isDefined: Boolean = !isEmpty
  type V
  def value: V
}

case object FailedVC extends ValueContainer {
  def isEmpty = true
  type V = Nothing
  def value = throw new NoSuchElementException("FailedVC.value")
}

case class BooleanVC(x: Boolean) extends ValueContainer {
  type V = Boolean
  def isEmpty = false
  def value = x
}

case class StringVC(x: String) extends ValueContainer {
  type V = String
  def isEmpty = false
  def value = x
}

case class JDocVC(x: JDoc) extends ValueContainer {
  type V = JDoc
  def isEmpty = false
  def value = x
}

case class MessageVC(x: Message) extends ValueContainer {
  type V = Message
  def isEmpty = false
  def value = x
}
case class TopicVC(x: Topic) extends ValueContainer {
  type V = Topic
  def isEmpty = false
  def value = x
}
