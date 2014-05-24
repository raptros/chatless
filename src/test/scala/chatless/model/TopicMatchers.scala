package chatless.model
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import argonaut.Json
import chatless.model.topic.{Topic, TopicMode, TopicInit}

trait TopicMatchers {
  def server(ev: String) = HavePropertyMatcher { t: Topic =>
    HavePropertyMatchResult(t.server == ev, "server", ev, t.server)
  }

  def user(ev: String) = HavePropertyMatcher { t: Topic =>
    HavePropertyMatchResult(t.user == ev, "user", ev, t.user)
  }

  def id(ev: String) = HavePropertyMatcher { t: Topic =>
    HavePropertyMatchResult(t.id == ev, "id", ev, t.id)
  }

  def banner(ev: String) = HavePropertyMatcher { t: Topic =>
    HavePropertyMatchResult(t.banner == ev, "banner", ev, t.banner)
  }

  def info(ev: Json) = HavePropertyMatcher { t: Topic =>
    HavePropertyMatchResult(t.info == ev, "info", ev, t.info)
  }

  def mode(ev: TopicMode) = HavePropertyMatcher { t: Topic =>
    HavePropertyMatchResult(t.mode == ev, "mode", ev, t.mode)
  }

  def coordinate(ev: TopicCoordinate) = HavePropertyMatcher { t: Topic =>
    HavePropertyMatchResult(t.coordinate == ev, "coordinate", ev, t.coordinate)
  }
}
