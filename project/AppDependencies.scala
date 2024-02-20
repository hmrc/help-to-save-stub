import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {
  val hmrc                    = "uk.gov.hmrc"
  val playVersion             = "play-30"
  val bootstrapBackendVersion = "8.4.0"

  val dependencies: Seq[ModuleID] = Seq(
    ws,
    hmrc                %% s"bootstrap-backend-$playVersion" % bootstrapBackendVersion,
    hmrc                %% s"domain-$playVersion"            % "9.0.0",
    hmrc                %% "stub-data-generator"             % "1.1.0",
    "org.typelevel"     %% "cats-core"                       % "2.10.0",
    "ai.x"              %% "play-json-extensions"            % "0.42.0",
    "com.github.kxbmap" %% "configs"                         % "0.6.1"
    //"com.google.inject" % "guice"                            % "5.0.1",
  )

  def testDependencies(scope: String = "test"): Seq[ModuleID] = Seq(
    hmrc                     %% s"bootstrap-test-$playVersion" % bootstrapBackendVersion % scope,
    "org.scalatestplus"      %% "scalatestplus-scalacheck"     % "3.1.0.0-RC2"           % scope,
    "org.scalacheck"         %% "scalacheck"                   % "1.17.0"                % scope,
    "com.github.pjfanning"   %% "pekko-mock-scheduler"    % "0.6.0"                 % scope,
    "org.pegdown"            % "pegdown"                       % "1.6.0"                 % scope
  )
}
