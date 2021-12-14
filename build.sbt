import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import play.core.PlayVersion
import sbt.Keys.compile
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import wartremover.{Wart, Warts}
import wartremover.WartRemover.autoImport.{wartremoverErrors, wartremoverExcluded}

lazy val appDependencies: Seq[ModuleID] = dependencies ++ testDependencies()
lazy val plugins: Seq[Plugins]          = Seq.empty
lazy val playSettings: Seq[Setting[_]]  = Seq.empty
lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*Reverse.*;.*config.*;.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}
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
    Wart.Var
  )

  Compile / compile / wartremoverErrors ++= Warts.allBut(excludedWarts: _*)
}
lazy val microservice =
  Project(appName, file("."))
    .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins: _*)
    .settings(addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17"))
    .settings(playSettings ++ scoverageSettings: _*)
    .settings(scalaSettings: _*)
    .settings(majorVersion := 2)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(scalaVersion := "2.12.11")
    .settings(PlayKeys.playDefaultPort := 7002)
    .settings(scalafmtOnCompile := true)
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
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(
      IntegrationTest / Keys.fork := false,
      IntegrationTest / unmanagedSourceDirectories := Seq((IntegrationTest / baseDirectory).value / "it"),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      IntegrationTest / parallelExecution := false
    )
    .settings(scalacOptions += "-P:silencer:pathFilters=routes")
    .settings(Global / lintUnusedKeysOnLoad := false)

val appName = "help-to-save-stub"
val hmrc    = "uk.gov.hmrc"
val dependencies = Seq(
  ws,
  hmrc                %% "bootstrap-backend-play-28" % "5.14.0",
  hmrc                %% "domain"                    % "6.2.0-play-28",
  hmrc                %% "stub-data-generator"       % "0.5.3",
  "org.scalacheck"    %% "scalacheck"                % "1.14.3",
  "org.typelevel"     %% "cats-core"                 % "2.3.1",
  "ai.x"              %% "play-json-extensions"      % "0.40.2",
  "com.github.kxbmap" %% "configs"                   % "0.4.4",
  "com.google.inject" % "guice"                      % "5.0.1",
  compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % "1.7.1" % Provided cross CrossVersion.full
)

def testDependencies(scope: String = "test") = Seq(
  hmrc                     %% "bootstrap-backend-play-28" % "5.14.0"            % scope,
  hmrc                     %% "service-integration-test"  % "1.1.0-play-28"     % scope,
  "org.scalatest"          %% "scalatest"                 % "3.2.9"             % scope,
  "com.vladsch.flexmark"   % "flexmark-all"               % "0.35.10"           % scope,
  "org.scalatestplus"      %% "scalatestplus-scalacheck"  % "3.1.0.0-RC2"       % scope,
  "org.scalatestplus.play" %% "scalatestplus-play"        % "5.1.0"             % scope,
  "com.typesafe.play"      %% "play-test"                 % PlayVersion.current % scope,
  "com.miguno.akka"        %% "akka-mock-scheduler"       % "0.5.5"             % scope,
  "org.pegdown"            % "pegdown"                    % "1.6.0"             % scope
)
