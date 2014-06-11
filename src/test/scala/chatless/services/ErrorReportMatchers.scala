package chatless.services
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import argonaut._

trait ErrorReportMatchers {
  def operation(expected: String) = HavePropertyMatcher { r: ErrorReport[_] =>
    HavePropertyMatchResult(
      r.operation == expected,
      "operation",
      expected,
      r.operation
    )
  }

  def reason[R: Manifest](expected: R) = HavePropertyMatcher { r: ErrorReport[R] =>
    HavePropertyMatchResult(
      r.reason == expected,
      "reason",
      expected,
      r.operation
    )
  }
}
