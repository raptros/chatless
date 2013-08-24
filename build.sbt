val sprayVersion = settingKey[String]("current version of spray")

organization := "chatless"

name := "chatless"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

seq(Revolver.settings: _*)

sprayVersion := "1.2-20130822"

showCurrentGitBranch

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

resolvers += "spray nightly repo" at "http://nightlies.spray.io"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.mongodb" %% "casbah" % "2.6.2",
  "org.scalaz" %% "scalaz-core" % "7.0.2",
  "com.chuusai" %% "shapeless" % "1.2.4",
  "io.argonaut" %% "argonaut" % "6.0-RC3",
  "io.spray" % "spray-can" % sprayVersion.value,
  "io.spray" % "spray-routing" % sprayVersion.value,
  "io.spray" % "spray-testkit" % sprayVersion.value % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.2.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.0" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta",
  "com.google.inject.extensions" % "guice-assistedinject" % "4.0-beta"
)

