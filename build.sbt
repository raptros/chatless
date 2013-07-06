organization := "chatless"

name := "chatless"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers += "spray repo" at "http://repo.spray.io"



libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.0.0",
  "com.chuusai" %% "shapeless" % "1.2.4",
  "org.json4s" %% "json4s-native" % "3.2.4",
  "io.spray" % "spray-can" % "1.2-M8",
  "io.spray" % "spray-routing" % "1.2-M8",
  "io.spray" % "spray-testkit" % "1.2-M8" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.2.0-RC1",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.0-RC1" % "test",
  "org.scalatest" % "scalatest_2.10" % "2.0.M5b" % "test"
)


seq(Revolver.settings: _*)