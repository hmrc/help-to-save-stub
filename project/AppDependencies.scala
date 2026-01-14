import sbt.*

object AppDependencies {
  val hmrc                    = "uk.gov.hmrc"
  val playVersion             = "play-30"
  val bootstrapBackendVersion = "10.5.0"

  val dependencies: Seq[ModuleID] = Seq(
    hmrc                %% s"bootstrap-backend-$playVersion" % bootstrapBackendVersion,
    hmrc                %% "stub-data-generator"             % "1.6.0"   %  "compile",
    "org.typelevel"     %% "cats-core"                       % "2.13.0"
  )

  def testDependencies(scope: String = "test"): Seq[ModuleID] = Seq(
    hmrc                   %% s"bootstrap-test-$playVersion" % bootstrapBackendVersion % scope,
    "com.github.pjfanning" %% "pekko-mock-scheduler"         % "0.6.0"                 % scope,
    "org.scalatestplus"    %% "scalacheck-1-17"              % "3.2.18.0"              % scope,
    hmrc                   %% s"domain-test-$playVersion"    % "13.0.0"                % scope
  )
}
