// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "rocksavage"

Test / parallelExecution := false

val chiselVersion = "5.0.0"
val scalafmtVersion = "2.5.0"

lazy val root = (project in file("."))
  .settings(
    name := "DynamicFifo",
    Test / publishArtifact := true,
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "5.0.0" % "test",
      "org.scalameta" % "sbt-scalafmt_2.12_1.0" % scalafmtVersion
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Ymacro-annotations",
    ),
    addCompilerPlugin(
      "org.chipsalliance" % 
      "chisel-plugin" % chiselVersion cross CrossVersion.full),
  )
