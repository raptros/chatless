package chatless

import org.scalatest.{Suite, Outcome, SuiteMixin}
import org.scalamock.MockFactoryBase
import org.scalatest.exceptions.TestFailedException

trait MockFactory2 extends SuiteMixin with MockFactoryBase { this: Suite =>

  type ExpectationException = TestFailedException

  protected def autoVerify = true

  override def withFixture(test: NoArgTest) = if (autoVerify)
      withExpectations { test() }
    else
      test()


  protected def newExpectationException(message: String, methodName: Option[Symbol]) =
    new TestFailedException(_ => Some(message), None, {e =>
        e.getStackTrace indexWhere { s =>
          !s.getClassName.startsWith("org.scalamock") && !s.getClassName.startsWith("org.scalatest") &&
          !(s.getMethodName == "newExpectationException") && !(s.getMethodName == "reportUnexpectedCall") &&
          !(methodName.isDefined && s.getMethodName == methodName.get.name)
        }
      })
}