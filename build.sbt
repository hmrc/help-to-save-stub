import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import play.core.PlayVersion
import sbt.Keys.compile
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import wartremover.{Wart, Warts, wartremoverErrors, wartremoverExcluded}

val appName = "help-to-save-stub"

lazy val appDependencies: Seq[ModuleID] = dependencies ++ testDependencies()
lazy val plugins: Seq[Plugins]          = Seq.empty
lazy val playSettings: Seq[Setting[_]]  = Seq.empty

val dependencies = Seq(
  ws,
  "uk.gov.hmrc"       %% "bootstrap-play-26"    % "1.1.0",
  "uk.gov.hmrc"       %% "play-config"          % "7.5.0",
  "uk.gov.hmrc"       %% "domain"               % "5.6.0-play-26",
  "org.scalacheck"    %% "scalacheck"           % "1.14.0",
  "org.typelevel"     %% "cats-core"            % "2.0.0",
  "uk.gov.hmrc"       %% "stub-data-generator"  % "0.5.3",
  "ai.x"              %% "play-json-extensions" % "0.10.0",
  "com.github.kxbmap" %% "configs"              % "0.4.4",
  "com.google.inject" % "guice"                 % "4.2.0"
)

def testDependencies(scope: String = "test,it") = Seq(
  "uk.gov.hmrc"       %% "bootstrap-play-26"   % "1.1.0"             % scope,
  "uk.gov.hmrc"       %% "hmrctest"            % "3.9.0-play-26"     % scope,
  "org.scalatest"     %% "scalatest"           % "3.0.8"             % scope,
  "com.typesafe.play" %% "play-test"           % PlayVersion.current % scope,
  "com.miguno.akka"   %% "akka-mock-scheduler" % "0.5.1"             % scope
)
lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*config.*;.*(AuthService|BuildInfo|Routes).*",
    //ScoverageKeys.coverageMinimum := 70,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
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

  wartremoverErrors in (Compile, compile) ++= Warts.allBut(excludedWarts: _*)
}

lazy val microservice =
  Project(appName, file("."))
    .enablePlugins(Seq(
      play.sbt.PlayScala,
      SbtAutoBuildPlugin,
      SbtGitVersioning,
      SbtDistributablesPlugin,
      SbtArtifactory) ++ plugins: _*)
    .settings(addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17"))
    .settings(playSettings ++ scoverageSettings: _*)
    .settings(scalaSettings: _*)
    .settings(majorVersion := 2)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(PlayKeys.playDefaultPort := 7002)
    .settings(scalafmtOnCompile := true)
    .settings(wartRemoverSettings)
    // disable some wart remover checks in tests - (Any, Null, PublicInference) seems to struggle with
    // scalamock, (Equals) seems to struggle with stub generator AutoGen and (NonUnitStatements) is
    // imcompatible with a lot of WordSpec
    .settings(wartremoverErrors in (Test, compile) --= Seq(
      Wart.Any,
      Wart.Equals,
      Wart.Null,
      Wart.NonUnitStatements,
      Wart.PublicInference))
    .settings(wartremoverExcluded ++=
      routes.in(Compile).value ++
        (baseDirectory.value ** "*.sc").get ++
        Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"))
    .settings(
      libraryDependencies ++= appDependencies,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .settings(scalacOptions += "-Xcheckinit")
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest := Seq((baseDirectory in IntegrationTest).value / "it"),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      //testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false
    )
    .settings(resolvers ++= Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.jcenterRepo
    ))
