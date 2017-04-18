/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package smartstub.people

import org.scalacheck.Gen
import Gen._

object Loader {
  def weighted(file: String): Gen[String] = {
    val resource = this.getClass.getResource(file)
    val data = scala.io.Source.fromURL(resource).getLines
    val nocomments = data.filterNot(_.startsWith("#"))
    val freqTuples = nocomments.map(_.split("\t").toList).collect {
      case (f::w::_) => (w.filter(_.isDigit).toInt, const(f))
    }.toSeq
    frequency(freqTuples :_*)
  }

  def apply(file: String): Gen[String] = {
    val resource = this.getClass.getResource(file)
    val data = scala.io.Source.fromURL(resource).getLines
    Gen.oneOf(data.filterNot(_.startsWith("#")).toList)
  }
}

object Names {
  lazy val surnames = Loader.weighted("/surnames.txt")
  lazy val forenames = Map(
    ( Male -> Loader("/forenames-male.txt")),
    ( Female -> Loader("/forenames-female.txt"))
  )

  def email(frags: String*): Gen[String] = for {
    spacer <- oneOf("_", "-", ".")
    numFrags <- choose(1, frags.size)
    es <- pick(numFrags, frags)
    domain <- alphaLowerStr.map(_.take(6))
    tld <- oneOf("com","co.uk","org","org.uk","gov.uk")
  } yield {
    s"${es.mkString(spacer)}@$domain.$tld"
  }

  def phoneNo: Gen[String] = for {
    prefix <- listOfN(4, numChar).map{"0" ++ _.mkString}
    suffix <- listOfN(6, numChar).map{_.mkString}
  } yield {
    s"$prefix $suffix"
  }
}

