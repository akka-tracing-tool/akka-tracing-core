val org = "pl.edu.agh.iet"
val appVersion = "0.2"

val Slf4jVersion = "1.7.24"
val ConfigVersion = "1.3.1"
val AkkaVersion = "2.4.17"
val ScalaTestVersion = "3.0.1"
val SlickVersion = "3.2.0"
val H2DatabaseVersion = "1.4.193"
val Json4sVersion = "3.5.1"

val UsedScalaVersion = "2.11.11"

name := "akka-tracing-core"

version := appVersion

organization := org

scalaVersion := UsedScalaVersion

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "org.json4s" %% "json4s-native" % Json4sVersion,
  "org.json4s" %% "json4s-ext" % Json4sVersion,
  "com.typesafe" % "config" % ConfigVersion,
  "org.slf4j" % "slf4j-api" % Slf4jVersion,
  "org.slf4j" % "slf4j-simple" % Slf4jVersion,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
)

lazy val core = project in file(".")
