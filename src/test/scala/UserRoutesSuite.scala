import org.scalatest.FunSuite
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

class UserRoutesSuite extends FunSuite with ScalatestRouteTest with HttpService {
  def actorRefFactory = system

}
