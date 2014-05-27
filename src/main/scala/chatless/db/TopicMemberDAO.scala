package chatless.db


import chatless._
import chatless.model._
import com.mongodb.casbah.Imports._
import scalaz._
import chatless.model.topic.MemberMode
import argonaut._

trait TopicMemberDAO {
  def get(tc: TopicCoordinate, uc: UserCoordinate)

}
