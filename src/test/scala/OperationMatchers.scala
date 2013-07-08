import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import chatless.db.{OpSpec, OpRes, Operation}

trait OperationMatchers {

  def cid(expectedValue:String) = new HavePropertyMatcher[Operation, String] {
    def apply(operation:Operation) = HavePropertyMatchResult(
      operation.cid == expectedValue,
      "cid",
      expectedValue,
      operation.cid)
  }

  def res(expectedValue:OpRes) = new HavePropertyMatcher[Operation, OpRes] {
    def apply(operation:Operation) = HavePropertyMatchResult(
      operation.res == expectedValue,
      "res",
      expectedValue,
      operation.res)
  }

  def opSpec(expectedValue:OpSpec) = new HavePropertyMatcher[Operation, OpSpec] {
    def apply(operation:Operation) = HavePropertyMatchResult(
      operation.opSpec == expectedValue,
      "opSpec",
      expectedValue,
      operation.opSpec)
  }
}
