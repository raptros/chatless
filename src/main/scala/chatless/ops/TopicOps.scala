package chatless.ops

import chatless.db._
import com.google.inject.Inject
import chatless.wiring.params.ServerIdParam
import chatless.model._
import chatless.model.topic.{Topic, Member, MemberMode, TopicInit}
import scalaz._
import scalaz.syntax.id._
import scalaz.syntax.traverse._
import scalaz.syntax.bitraverse._
import scalaz.syntax.monoid._
import scalaz.syntax.state._
import scalaz.syntax.applicative._
import scalaz.syntax.std.boolean._
import spray.http.HttpResponse
import scalaz.\/._
import scalaz.std.anyVal._

trait TopicOps {
  def createTopic(caller: User, init: TopicInit): OperationResult[Created[Topic]]

  def inviteUser(topic: Topic, inviter: User, user: User): OperationResult[Member]

  def getMembers(caller: User, topic: Topic): OperationResult[List[(UserCoordinate, MemberMode)]]

  def userCanRead(user: User, topic: Topic): OperationResult[Boolean]
}

class TopicOpsImpl @Inject() (
    @ServerIdParam
    serverId: ServerCoordinate,
    userDao: UserDAO,
    topicDao: TopicDAO,
    messageDao: MessageDAO,
    topicMemberDao: TopicMemberDAO)
  extends TopicOps {
  import TopicOps._

  def createTopic(caller: User, init: TopicInit): OperationResult[Created[Topic]] = for {
    _ <- UserNotLocal(caller.coordinate, serverId).left unlessM (caller.server == serverId.server)
    topic <- topicDao.createLocal(caller.id, init) leftMap {
      CreateTopicFailed(caller.coordinate, init, _)
    }
    member <- topicMemberDao.set(topic.coordinate, caller.coordinate, MemberMode.creator) leftMap {
      SetFirstMemberFailed(topic.coordinate, _)
    }
  } yield Created(topic)

  def inviteUser(topic: Topic, inviter: User, user: User): OperationResult[Member] = for {
    member <- topicMemberDao.set(topic.coordinate, user.coordinate, MemberMode.invitedMode(topic.mode)) leftMap {
      AddMemberFailed(topic.coordinate, user.coordinate, _)
    }
    //todo: somehow tell the invitee about the invitation
  } yield member

  def userCanRead(user: User, topic: Topic): OperationResult[Boolean] = for {
    userMembership <- topicMemberDao.get(topic.coordinate, user.coordinate) leftMap {
      GetMembershipFailed(topic.coordinate, user.coordinate, _)
    }
  } yield true

  override def getMembers(caller: User, topic: Topic) = for {
    canRead <- userCanRead(caller, topic)
    _ <- UserReadDenied(topic.coordinate, caller.coordinate).left unlessM canRead
  } yield Nil
}

object TopicOps {
  /*
  type EitherTReader[A, +L, +R] = EitherT[({type r[+b] = Reader[A, b]})#r, L, R]

  object EitherTReader extends EitherTInstances with EitherTFunctions {
    def apply[A, L, R](f: A => L \/ R): EitherTReader[A, L, R] = EitherT[({type r[+b] = Reader[A, b]})#r, L, R] {
      Reader(f)
    }
  }

  type UnitLeft[A] = Unit \/ A
  type UnitRight[A] = A \/ Unit

  implicit def LeftMonoidDisjPlusEmpty[L: Monoid]: PlusEmpty[({type l[a] = L \/ a})#l] = new PlusEmpty[({type l[a] = L \/ a})#l] {
    def empty[A]: L \/ A = -\/(Monoid[L].zero)

    def plus[A](a: L \/ A, b: => L \/ A): L \/ A = a orElse b
  }

  implicit def RightMonoidApplicativePlus[R: Monoid]: ApplicativePlus[({type r[a] = a \/ R})#r] = new ApplicativePlus[({type r[a] = \/[a, R]})#r] {
    override def point[A](a: => A): (A \/ R) = -\/(a)

    override def empty[A]: (A \/ R) = \/-(Monoid[R].zero)

    override def plus[A](a: (A \/ R), b: => (A \/ R)): (A \/ R) = ~(~a orElse ~b)

    override def ap[A, B](fa: => A \/ R)(f: => \/[A => B, R]): B \/ R = fa match {
      case -\/(a) => f.bimap(lf => lf(a), identity)
      case \/-(b) => \/-(b)
    }
  }
*/

}