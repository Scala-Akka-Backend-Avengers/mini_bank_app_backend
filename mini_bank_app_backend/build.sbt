ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor-typed",
  "com.typesafe.akka" %% "akka-stream",
  "com.typesafe.akka" %% "akka-persistence-typed",
  "com.typesafe.akka" %% "akka-persistence-query",
) map (_ % "2.6.9")

lazy val akkaTestDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor-testkit-typed",
  "com.typesafe.akka" %% "akka-stream-testkit",
  "com.typesafe.akka" %% "akka-persistence-testkit",
) map (_ % "2.6.9" % Test)

lazy val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.0.6"

lazy val javaCoreDriver = "com.datastax.oss" % "java-driver-core" % "4.13.0"

lazy val akkaHttpDependency = "com.typesafe.akka" %% "akka-http" % "10.2.10"

lazy val akkaHttpTestDependency = "com.typesafe.akka" %% "akka-http-testkit" % "10.2.10" % Test

lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
) map (_  % "0.14.1")

lazy val akkaHttpCirceDependency = "de.heikoseeberger" %% "akka-http-circe" % "1.39.2"

lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.11"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.16" % Test

lazy val root = (project in file("."))
  .settings(
    name := "mini_bank_app_backend",
    idePackagePrefix := Some("com.fun.mini_bank"),
    libraryDependencies ++=
      akkaDependencies
      ++ circeDependencies
      ++ akkaTestDependencies
      ++ Seq(
        akkaHttpDependency,
        akkaPersistenceCassandra,
        javaCoreDriver,
        akkaHttpCirceDependency,
        logbackClassic,
        akkaHttpTestDependency,
        scalaTest
      )
  )
