// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "tech.rocksavage"
ThisBuild / organizationName := "Rocksavage Technology"

Test / parallelExecution := false

val chiselVersion   = "5.0.0"
val scalafmtVersion = "2.5.0"

lazy val root = (project in file("."))
  .settings(
    name                   := "dynamicfifo",
    Test / publishArtifact := true,
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel"     % chiselVersion,
      "edu.berkeley.cs"   %% "chiseltest" % "5.0.0" % "test",
      "tech.rocksavage"   %% "chiselware" % "0.1.0",
      "tech.rocksavage"   %% "chiselware" % "0.1.0" % "test" classifier "tests"
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Ymacro-annotations"
    ),
    addCompilerPlugin(
      "org.chipsalliance" % "chisel-plugin" % "5.0.0" cross CrossVersion.full
    )
  )

// Scala coverage settings
coverageDataDir            := target.value / "../generated/scalaCoverage"
coverageFailOnMinimum      := true
coverageMinimumStmtTotal   := 90
coverageMinimumBranchTotal := 90
