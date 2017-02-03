name := """lunatech-random-repo"""

version := "1.0"

scalaVersion := "2.11.7"

lazy val fs2Version = "0.9.2"
lazy val circeVersion = "0.5.1"
lazy val http4sVersion = "0.14.6"

libraryDependencies ++= Seq(
  "co.fs2"          %% "fs2-core"   % fs2Version,
  "co.fs2"          %% "fs2-io"     % fs2Version,
  "org.http4s"      %% "http4s-dsl"           % http4sVersion,
  "org.http4s"      %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s"      %% "http4s-blaze-client"  % http4sVersion,
  "org.http4s"      %% "http4s-circe"         % http4sVersion,
  "io.circe"        %% "circe-core"           % circeVersion,
  "io.circe"        %% "circe-generic"        % circeVersion,
  "io.circe"        %% "circe-parser"         % circeVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-core" % "5.0.1" exclude("org.scalacheck", "scalacheck_2.11"),
  "org.scalatest"   %% "scalatest"  % "2.2.6" % "test",
  "org.scalacheck"  %% "scalacheck" % "1.12.1" % "test"
)

