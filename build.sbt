val org = "pl.edu.agh.iet"
val appVersion = "0.0.1"

val Slf4jVersion = "1.7.12"
val ConfigVersion = "1.3.0"
val AkkaVersion = "2.3.9"
val ScalaTestVersion = "2.2.4"
val SlickVersion = "3.0.0"

name := "akka-tracing-core"

version := appVersion

organization := org

crossScalaVersions := Seq("2.10.5", "2.11.7")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
  "com.typesafe" % "config" % ConfigVersion,
  "com.typesafe.slick" %% "slick" % SlickVersion,
  "org.slf4j" % "slf4j-api" % Slf4jVersion,
  "org.slf4j" % "log4j-over-slf4j" % Slf4jVersion % "test",
  "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"
)

lazy val coreProject = project in file(".")
