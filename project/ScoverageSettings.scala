import sbt.Keys.parallelExecution
import sbt.{Def, Test}
import scoverage.ScoverageKeys

object ScoverageSettings {
  lazy val scoverageSettings: Seq[Def.Setting[? >: String & Double & Boolean]] = Seq(
    // Semicolon-separated list of regexes matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*Reverse.*;.*config.*;.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimumStmtTotal := 85,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}
