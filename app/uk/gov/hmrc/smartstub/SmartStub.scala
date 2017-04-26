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

package smartstub

import uk.gov.hmrc.helptosavestub.models._
import java.time.ZoneOffset

import scala.collection.mutable.{Map => MutableMap}
import java.time.LocalDate
import org.scalacheck._

trait SmartStubGenerator[IN,OUT] {
  val Gen = org.scalacheck.Gen
  val state = MutableMap.empty[IN,OUT]
  def apply(in: IN): Option[OUT] = state.get(in) orElse generate(in)
  def generate(in: IN): Option[OUT] =
    from(in).flatMap{x => generator(in)(Gen.Parameters.default, rng.Seed(x))}

  def generator(in: IN): Gen[OUT]

  def dateGen(
    start: Int = 1970,
    end: Int = 2000
  ): Gen[LocalDate] = dateGen(
    LocalDate.of(start, 1, 1),
    LocalDate.of(end, 12, 31)
  )

  def dateGen(start: LocalDate, end: LocalDate): Gen[LocalDate] = 
    Gen.choose(start.toEpochDay, end.toEpochDay).map(LocalDate.ofEpochDay)

  def from(in: IN): Option[Long]

  def fromNino(in: String): Option[Long] = {
    val numSeq: Seq[BigInt] = in.toUpperCase.collect{
      case d if d.isDigit => BigInt(d.toString)
      case l if l.isLetter => BigInt(l.toInt - 65)
    }
    val num = numSeq.zipWithIndex.map{case (v, e) => v * BigInt(24).pow(e + 1)}.sum
    Some(num.toLong)
  }
}
