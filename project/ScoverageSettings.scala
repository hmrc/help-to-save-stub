import sbt.Keys.parallelExecution
import sbt.{Def, Test}
import scoverage.ScoverageKeys

object ScoverageSettings {
  lazy val scoverageSettings: Seq[Def.Setting[_ >: String with Double with Boolean]] = Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*Reverse.*;.*config.*;.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimumStmtTotal := 85,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}
