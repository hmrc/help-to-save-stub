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

package uk.gov.hmrc.helptosavestub.controllers

import java.time.LocalDate

import org.scalacheck.Gen
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Action
import hmrc.smartstub._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.helptosavestub.models.NSIUserInfo.postcodeRegex

object CitizenDetailsController extends BaseController {

  type NINO = String

  case class Person(firstName: Option[String],
                    lastName: Option[String],
                    dateOfBirth: Option[LocalDate])

  case class Address(line1: Option[String],
                     line2: Option[String],
                     line3: Option[String],
                     postcode: Option[String],
                     country: Option[String]
                    )

  case class Response(person: Option[Person], address: Option[Address])

  implicit val personWrites: Writes[Person] = Json.writes[Person]
  implicit val addressWrites: Writes[Address] = Json.writes[Address]
  implicit val responseWrites: Writes[Response] = Json.writes[Response]

  val responseGen: Gen[Response] = {
    import Gen._

    /**
      * Return [[Some]] 99% of the time and [[None]] 1% of the time
      */
    def usually[A](a: Gen[A]): Gen[Option[A]] =
      Gen.frequency(99 → a.map(Some(_)), 1 → const[Option[A]](None))

    val personGen = for {
      fnameO <- usually(Gen.forename)
      snameO <- usually(Gen.surname)
      dobO <- option(Gen.date(1940, 2017))
    } yield Person( fnameO, snameO, dobO ) 

    val addressGen = for {
      address <- Gen.ukAddress.map{_.map{x => usually(const(x))}}
      initAdd = address.init ++ List.fill(3)(const(Option.empty[String]))
      add1 <- initAdd(0)
      add2 <- initAdd(1)
      add3 <- initAdd(2)
      postcodeO <- address.last
      countryO <- usually(const("UK"))
    } yield Address(add1, add2, add3, postcodeO, countryO)

    for {
      personO <- usually(personGen)
      addressO <- usually(addressGen)
    } yield Response( personO, addressO )
  }

  def retrieveDetails(nino: NINO) = Action { implicit request =>
    implicit val ninoEnum: Enumerable[String] = pattern"ZZ999999Z"
    responseGen.seeded(nino).map { response =>
      Ok(Json.toJson(response))
    }.getOrElse{
      NotFound
    }
  }

}
