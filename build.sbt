import AppDependencies.*
import ScoverageSettings.scoverageSettings
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}

val appName = "help-to-save-stub"

lazy val appDependencies: Seq[ModuleID] = dependencies ++ testDependencies()
lazy val ItTest = config("it") extend Test

lazy val microservice =
  Project(appName, file("."))
    .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
    .settings(scoverageSettings: _*)
    .settings(scalaSettings: _*)
    .settings(majorVersion := 2)
    .settings(defaultSettings(): _*)
    .settings(scalaVersion := "2.13.12")
    .settings(PlayKeys.playDefaultPort := 7002)
    .settings(
      libraryDependencies ++= appDependencies,
      retrieveManaged := true,
      update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .settings(scalacOptions ++= Seq("-Xcheckinit", "-feature"))
    .configs(ItTest)
    .settings(inConfig(ItTest)(Defaults.testSettings): _*)
    .settings(
      ItTest / Keys.fork := false,
      ItTest / unmanagedSourceDirectories := Seq((ItTest / baseDirectory).value / "it"),
      addTestReportOption(ItTest, "int-test-reports"),
      ItTest / parallelExecution := false
    )
    .settings(scalacOptions += "-Wconf:src=routes/.*:s")
    .settings(Global / lintUnusedKeysOnLoad := false)
