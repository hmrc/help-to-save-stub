import AppDependencies._
import ScoverageSettings.scoverageSettings
import sbt.Keys.compile
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import wartremover.WartRemover.autoImport.{wartremoverErrors, wartremoverExcluded}
import wartremover.{Wart, Warts}

val appName = "help-to-save-stub"

lazy val appDependencies: Seq[ModuleID] = dependencies ++ testDependencies()
lazy val plugins: Seq[Plugins]          = Seq.empty
lazy val playSettings: Seq[Setting[_]]  = Seq.empty

lazy val ItTest = config("it") extend Test

lazy val wartRemoverSettings = {
  // list of warts here: http://www.wartremover.org/doc/warts.html
  val excludedWarts = Seq(
    Wart.DefaultArguments,
    Wart.FinalCaseClass,
    Wart.FinalVal,
    Wart.ImplicitConversion,
    Wart.ImplicitParameter,
    Wart.LeakingSealed,
    Wart.Nothing,
    Wart.Overloading,
    Wart.ToString,
    Wart.Var,
    Wart.StringPlusAny,
    Wart.ThreadSleep,
    Wart.Any,
    Wart.PlatformDefault,
  )

  Compile / compile / wartremoverErrors ++= Warts.allBut(excludedWarts: _*)
}

lazy val microservice =
  Project(appName, file("."))
    .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins: _*)
    .settings(playSettings ++ scoverageSettings: _*)
    .settings(scalaSettings: _*)
    .settings(majorVersion := 2)
    .settings(defaultSettings(): _*)
    .settings(scalaVersion := "2.13.12")
    .settings(PlayKeys.playDefaultPort := 7002)
    .settings(wartRemoverSettings)
    // disable some wart remover checks in tests - (Any, Null, PublicInference) seems to struggle with
    // scalamock, (Equals) seems to struggle with stub generator AutoGen and (NonUnitStatements) is
    // imcompatible with a lot of WordSpec
    .settings(Test / compile / wartremoverErrors --= Seq(
      Wart.Any,
      Wart.Equals,
      Wart.Null,
      Wart.NonUnitStatements,
      Wart.PublicInference))
    .settings(wartremoverExcluded ++=
      (Compile / routes).value ++
        (baseDirectory.value ** "*.sc").get ++
        Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"))
    .settings(
      libraryDependencies ++= appDependencies,
      retrieveManaged := true,
      update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .settings(scalacOptions ++= Seq("-Xcheckinit", "-feature"))
    .settings(Compile / scalacOptions -= "utf8")
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
