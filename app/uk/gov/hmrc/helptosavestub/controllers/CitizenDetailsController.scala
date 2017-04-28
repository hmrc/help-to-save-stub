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

  case class Address(line1: String,
                     line2: String,
                     line3: String,
                     postcode: String,
                     country: String
                    )

  case class Response(address: Address)

  implicit val addressWrites: Writes[Address] = Json.writes[Address]
  implicit val responseWrites: Writes[Response] = Json.writes[Response]

  object ResponseGenerator extends SmartStubGenerator[NINO, Response] {

    def from(in: NINO): Option[Long] = fromNino(in)

    def generator(in: NINO): Gen[Response] = addressGenerator.map(Response.apply)

    val addressGenerator: Gen[Address] = for {
      line1     ← Gen.alphaStr
      line2     ← Gen.alphaStr
      line3     ← Gen.alphaStr
      postcode  ← Gen.alphaNumStr
      country   ← Gen.identifier
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
