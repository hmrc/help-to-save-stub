package uk.gov.hmrc.helptosavestub

import scala.util.matching.Regex

package object util {

  val ninoRegex: Regex = """[A-Za-z]{2}[0-9]{6}[A-Za-z]{1}""".r

}