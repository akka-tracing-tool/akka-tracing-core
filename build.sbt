val org = "pl.edu.agh.iet"
val appVersion = "0.0.3"

val Slf4jVersion = "1.7.24"
val ConfigVersion = "1.3.1"
val AkkaVersion = "2.4.17"
val ScalaTestVersion = "3.0.1"
val SlickVersion = "3.2.0"
val H2DatabaseVersion = "1.4.193"

val UsedScalaVersion = "2.11.8"

name := "akka-tracing-core"

version := appVersion

organization := org

crossScalaVersions := Seq("2.10.5", UsedScalaVersion)

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
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
  "com.h2database" % "h2" % H2DatabaseVersion % Test
)

lazy val core = project in file(".")
