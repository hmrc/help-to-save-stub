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
import smartstub.SmartStubGenerator
import uk.gov.hmrc.play.microservice.controller.BaseController


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

  implicit val addressWrites: Writes[Address] = Json.writes[Address]
  implicit val responseWrites: Writes[Response] = Json.writes[Response]

  object ResponseGenerator extends SmartStubGenerator[NINO, Response] {

    def from(in: NINO): Option[Long] = fromNino(in)

    def generator(in: NINO): Gen[Response] =
      optionGen(addressGenerator).flatMap(a ⇒
        optionGen(personGenerator).map(p ⇒
          Response(p, a)))

    /**
      * Return [[Some]] 90% of the time and [[None]] 1% of the time
      */
    def optionGen[A](a: Gen[A]): Gen[Option[A]] =
      Gen.frequency(99 → a.map(Some(_)), 1 → Gen.const[Option[A]](None))

    val personGenerator: Gen[Person] = for{
       firstName   ← optionGen(Gen.identifier)
       lastName    ← optionGen(Gen.identifier)
       dateOfBirth ← optionGen(dateGen())
    } yield Person(firstName, lastName, dateOfBirth)

    val addressGenerator: Gen[Address] = for {
      line1     ← optionGen(Gen.alphaStr)
      line2     ← optionGen(Gen.alphaStr)
      line3     ← optionGen(Gen.alphaStr)
      postcode  ← optionGen(Gen.alphaNumStr)
      country   ← optionGen(Gen.identifier)
    } yield Address(line1, line2, line3, postcode, country)
  }

  def retrieveDetails(nino: NINO) = Action { implicit request =>
    ResponseGenerator(nino).map { response =>
      Ok(Json.toJson(response))
    }.getOrElse{
      NotFound
    }
  }

}
