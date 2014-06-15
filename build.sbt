val sprayVersion = settingKey[String]("current version of spray")
val akkaVersion = settingKey[String]("current version of akka")

lazy val buildSettings = Defaults.defaultSettings ++ Seq(
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "io.argonaut" %% "argonaut" % "6.0.4" withSources()
  ),
  scalaVersion := "2.11.1",
  crossScalaVersions := Seq(scalaVersion.value),
  startYear := Some(2014),
  organization := "chatless"
)

lazy val macros = project settings (buildSettings: _*)

lazy val chatless = (project in file(".")) settings (buildSettings: _*) dependsOn macros

name := "chatless"

version := "0.1-SNAPSHOT"

seq(Revolver.settings: _*)

sprayVersion := "1.3.1-20140423"

akkaVersion := "2.3.3"

showCurrentGitBranch

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io",
  "spray nightly repo" at "http://nightlies.spray.io",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion.value,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion.value,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion.value % "test"
)

libraryDependencies ++= Seq(
  "io.spray" %% "spray-io" % sprayVersion.value withJavadoc(),
  "io.spray" %% "spray-can" % sprayVersion.value withJavadoc(),
  "io.spray" %% "spray-http" % sprayVersion.value withJavadoc(),
  "io.spray" %% "spray-httpx" % sprayVersion.value withJavadoc(),
  "io.spray" %% "spray-routing" % sprayVersion.value withJavadoc(),
  "io.spray" %% "spray-testkit" % sprayVersion.value % "test"
)

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.7.2",
  "org.scalaz" %% "scalaz-core" % "7.0.6" withSources(),
  "com.chuusai" %% "shapeless" % "1.2.4",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta4",
  "com.google.inject.extensions" % "guice-assistedinject" % "4.0-beta",
  "io.github.raptros" %% "the-bson" % "0.1"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.5" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.1.1" % "test"
)

