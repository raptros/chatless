package chatless.model

import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import argonaut.Json
import chatless.model.topic.{TopicMode, TopicInit}

trait TopicInitMatchers {
  def fixedId(expectedValue: Option[String]) = HavePropertyMatcher { ti: TopicInit =>
    HavePropertyMatchResult(ti.fixedId == expectedValue, "fixedId", expectedValue, ti.fixedId)
  }

  def banner(expectedValue: String) = HavePropertyMatcher { ti: TopicInit =>
    HavePropertyMatchResult(ti.banner == expectedValue, "banner", expectedValue, ti.banner)
  }

  def info(expectedValue: Json) = HavePropertyMatcher { ti: TopicInit =>
    HavePropertyMatchResult(ti.info == expectedValue, "info", expectedValue, ti.info)
  }

  def mode(expectedValue: TopicMode) = HavePropertyMatcher { ti: TopicInit =>
    HavePropertyMatchResult(ti.mode == expectedValue, "mode", expectedValue, ti.mode)
  }
}
