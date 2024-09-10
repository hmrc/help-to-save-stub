import AppDependencies.*
import ScoverageSettings.scoverageSettings

val appName = "help-to-save-stub"

lazy val appDependencies: Seq[ModuleID] = dependencies ++ testDependencies()

lazy val microservice =
  Project(appName, file("."))
    .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
    .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
    .settings(scoverageSettings *)
    .settings(majorVersion := 2)
    .settings(scalaVersion := "2.13.12")
    .settings(PlayKeys.playDefaultPort := 7002)
    .settings(libraryDependencies ++= appDependencies)
    .settings(scalacOptions += "-Wconf:src=routes/.*:s")
