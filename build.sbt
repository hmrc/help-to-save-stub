import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import play.core.PlayVersion
import sbt.Keys.compile
import sbt.Keys.dependencyOverrides
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import wartremover.{Wart, Warts, wartremoverErrors, wartremoverExcluded}

lazy val appDependencies: Seq[ModuleID] = dependencies ++ overrides ++ testDependencies()
lazy val plugins: Seq[Plugins]          = Seq.empty
lazy val playSettings: Seq[Setting[_]]  = Seq.empty
lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*Reverse.*;.*config.*;.*(AuthService|BuildInfo|Routes).*",
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
    .settings(scalaVersion := "2.12.11")
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
    .settings(scalacOptions ++= Seq("-Xcheckinit", "-feature"))
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
val appName = "help-to-save-stub"
val hmrc    = "uk.gov.hmrc"
val dependencies = Seq(
  ws,
  hmrc                %% "bootstrap-backend-play-26" % "3.0.0",
  hmrc                %% "domain"                    % "5.10.0-play-26",
  hmrc                %% "stub-data-generator"       % "0.5.3",
  "org.scalacheck"    %% "scalacheck"                % "1.14.3",
  "org.typelevel"     %% "cats-core"                 % "2.2.0",
  "ai.x"              %% "play-json-extensions"      % "0.40.2",
  "com.github.kxbmap" %% "configs"                   % "0.4.4",
  "com.google.inject" % "guice"                      % "4.2.2"
)

val akkaVersion     = "2.5.23"
val akkaHttpVersion = "10.0.15"
val overrides = Seq(
  "com.typesafe.akka" %% "akka-stream"    % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf"  % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,
  "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
)

def testDependencies(scope: String = "test") = Seq(
  hmrc                     %% "service-integration-test" % "0.12.0-play-26"    % scope,
  "org.scalatest"          %% "scalatest"                % "3.2.0"             % scope,
  "com.vladsch.flexmark"   % "flexmark-all"              % "0.35.10"           % scope,
  "org.scalatestplus"      %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"       % scope,
  "org.scalatestplus.play" %% "scalatestplus-play"       % "3.1.3"             % scope,
  "com.typesafe.play"      %% "play-test"                % PlayVersion.current % scope,
  "com.miguno.akka"        %% "akka-mock-scheduler"      % "0.5.5"             % scope,
  "org.pegdown"            % "pegdown"                   % "1.6.0"             % scope
)
