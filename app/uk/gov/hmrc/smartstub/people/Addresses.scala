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

object Address {
  lazy val streetNames = Loader("/streets.txt")
  lazy val postcodeRegions = Loader("/postcodes.txt").map{
    x => (x.split(":").head, x.dropWhile(_ != ':').tail)
  }
  
  lazy val postcode = {
    def chars(n: Int) = listOfN(n, alphaUpperChar).map(_.mkString)
    def digits(n: Int) = listOfN(n, numChar).map(_.mkString)
    for {
      one <- chars(2)
      two <- digits(2)
      three <- numChar
      four <- chars(2)
    } yield s"$one$two $three$four"
  }

  def ukAddress : Gen[List[String]] = for {
    addressLetter <- frequency(
      (50,None),(5,Some("A")),(5,Some("B")),(3,Some("C"))
    )
    addressNumber <- choose(1,150)
    (codePrefix,town) <- postcodeRegions
    street <- streetNames
    postcode <- listOfN(2,alphaUpperChar).map(_.mkString).flatMap{
      n => listOfN(3,choose(0,9)).map(_.mkString).map{
        x => s"${codePrefix}${x.init} ${x.last}${n}"
      }
    }// .retryUntil(_.matches(Address.postcodeRegex))
  } yield List(
    addressNumber.toString() ++ addressLetter.getOrElse(""),
    street,
    town,
    postcode
  )  
}
