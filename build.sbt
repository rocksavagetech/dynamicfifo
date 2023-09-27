// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "rocksavage"

Test / parallelExecution := false

val chiselVersion = "3.5.4"
val scalafmtVersion = "2.5.0"

lazy val root = (project in file("."))
  .settings(
    name := "DynamicFifo",
    Test / publishArtifact := true,
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "0.5.4" % "test",
      "org.scalameta" % "sbt-scalafmt_2.12_1.0" % scalafmtVersion
    ),
    scalacOptions ++= Seq(
      "-Xsource:2.13",
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit"
    ),
  )
