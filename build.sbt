organization := "chatless"

name := "chatless"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers += "spray repo" at "http://repo.spray.io"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.mongodb" %% "casbah" % "2.6.2",
  "org.scalaz" %% "scalaz-core" % "7.0.2",
  "com.chuusai" %% "shapeless" % "1.2.4",
  "io.argonaut" %% "argonaut" % "6.0-RC3",
  "io.spray" % "spray-can" % "1.2-M8",
  "io.spray" % "spray-routing" % "1.2-M8",
  "io.spray" % "spray-testkit" % "1.2-M8" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.2.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.0" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)

showCurrentGitBranch

seq(Revolver.settings: _*)
