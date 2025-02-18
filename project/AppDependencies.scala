import sbt.*

object AppDependencies {
  val hmrc                    = "uk.gov.hmrc"
  val playVersion             = "play-30"
  val bootstrapBackendVersion = "8.6.0"

  val dependencies: Seq[ModuleID] = Seq(
    hmrc                %% s"bootstrap-backend-$playVersion" % bootstrapBackendVersion,
    hmrc                %% s"domain-$playVersion"            % "9.0.0",
    hmrc                %% "stub-data-generator"             % "1.2.0",
    "org.typelevel"     %% "cats-core"                       % "2.12.0",
    "com.github.kxbmap" %% "configs"                         % "0.6.1"
  )

  def testDependencies(scope: String = "test"): Seq[ModuleID] = Seq(
    hmrc                   %% s"bootstrap-test-$playVersion" % bootstrapBackendVersion % scope,
    "com.github.pjfanning" %% "pekko-mock-scheduler"         % "0.6.0"                 % scope
  )
}
