package chatless.chatservtests

import chatless._
import chatless.ServiceSpecBase
import org.scalatest.{Status, Args, Suite, Filter}
import scala.collection.immutable.IndexedSeq
import spray.routing.{RequestContext, HttpService}
import chatless.services.ServiceBase
import akka.actor.{ActorSystem, ActorSelection, ActorRefFactory}
import scala.concurrent.{Future, ExecutionContext}
import chatless.db.DatabaseAccessor
import spray.routing.authentication.BasicAuth
import spray.http.BasicHttpCredentials
import spray.routing.authentication.{UserPass, UserPassAuthenticator, BasicAuth, ContextAuthenticator}

class TestableRoute(val dbac:DatabaseAccessor)(implicit val system:ActorSystem) extends HttpService with ServiceBase {
  def getUserAuth:ContextAuthenticator[UserId] = BasicAuth(upa, "")

  /** won't ever actually be called for*/
  def dbSel = system.actorSelection("../chatless-service-db")


  val userId = s"test-${this.getClass.getSimpleName}-123"
  val password = "hypermachinery"

  /**fake auth stuff for testing*/
  val upa:UserPassAuthenticator[UserId] = { (oup:Option[UserPass]) =>
    Future.successful {
      for {
        UserPass(uid, pass) <- oup
        if uid == userId && pass == password
      } yield uid
    }
  }

  implicit def executor:ExecutionContext = system.dispatcher

  implicit def actorRefFactory:ActorRefFactory = system
}
