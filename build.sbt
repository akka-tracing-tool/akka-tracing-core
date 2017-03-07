val org = "pl.edu.agh.iet"
val appVersion = "0.0.3"

val Slf4jVersion = "1.7.24"
val ConfigVersion = "1.3.0"
val AkkaVersion = "2.4.17"
val ScalaTestVersion = "3.0.1"
val SlickVersion = "3.2.0"
val UsedScalaVersion = "2.11.8"

name := "akka-tracing-core"

version := appVersion

organization := org

crossScalaVersions := Seq("2.10.5", "2.11.8")

scalaVersion := UsedScalaVersion

scalacOptions ++= Seq("-feature", "-deprecation")

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
