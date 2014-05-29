package chatless.db

import chatless.model._
import scalaz._
import chatless.model.topic.{Member, MemberMode}

trait TopicMemberDAO {
  def get(topic: TopicCoordinate, user: UserCoordinate): DbResult[Option[Member]]

  def set(topic: TopicCoordinate, user: UserCoordinate, mode: MemberMode): DbResult[Member]

  def set(member: Member): DbResult[Member] = set(member.topic, member.user, member.mode)

  def list(topic: TopicCoordinate): DbResult[Seq[Member]]
}
