package chatless.model

import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import argonaut.Json

trait TopicInitMatchers {
  def fixedId(expectedValue: Option[String]) = new HavePropertyMatcher[TopicInit, Option[String]] {
    override def apply(ti: TopicInit) =
      HavePropertyMatchResult(ti.fixedId == expectedValue, "fixedId", expectedValue, ti.fixedId)
  }

  def banner(expectedValue: String) = new HavePropertyMatcher[TopicInit, String] {
    override def apply(ti: TopicInit) =
      HavePropertyMatchResult(ti.banner == expectedValue, "banner", expectedValue, ti.banner)
  }

  def info(expectedValue: Json) = new HavePropertyMatcher[TopicInit, Json] {
    override def apply(ti: TopicInit) =
      HavePropertyMatchResult(ti.info == expectedValue, "info", expectedValue, ti.info)
  }

  def invite(expectedValue: List[UserCoordinate]) = new HavePropertyMatcher[TopicInit, List[UserCoordinate]] {
    override def apply(ti: TopicInit) =
      HavePropertyMatchResult(ti.invite == expectedValue, "invite", expectedValue, ti.invite)
  }
}
