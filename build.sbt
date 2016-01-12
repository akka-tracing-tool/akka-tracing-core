val org = "pl.edu.agh.iet"
val appVersion = "0.0.2"

val Slf4jVersion = "1.7.12"
val ConfigVersion = "1.3.0"
val AkkaVersion = "2.3.9"
val ScalaTestVersion = "2.2.4"
val SlickVersion = "3.1.1"
val UsedScalaVersion = "2.11.7"

name := "akka-tracing-core"

version := appVersion

organization := org

crossScalaVersions := Seq("2.10.5", "2.11.7")

scalaVersion := UsedScalaVersion

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion,
  "com.typesafe" % "config" % ConfigVersion,
  "com.typesafe.slick" %% "slick" % SlickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion,
  "org.slf4j" % "slf4j-api" % Slf4jVersion,
  "org.slf4j" % "slf4j-simple" % Slf4jVersion,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
  "com.h2database" % "h2" % "1.4.190" % Test
)

lazy val coreProject = project in file(".")
