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
import play.api.libs.json.Json
import play.api.mvc._
import smartstub._
import uk.gov.hmrc.play.microservice.controller.BaseController

object ItmpController extends BaseController {

  type Address = List[String]
  type Email = String

  case class H2SEligibility (
    name: String,
    dob: LocalDate,
    address: Option[Address],
    email: Email,
    overallEligibility: Boolean,
    wtcEligibility: Boolean,
    ucEligibility: Boolean,
    weeklyHouseholdIncome: Int
  )

  object UserDetailsGenerator extends SmartStubGenerator[String, H2SEligibility] {
    import org.scalacheck.Gen._

    def from(in: String): Option[Long] =
      fromNino(in)

    def generator(in: String) = for {
      gender <- oneOf(people.Male, people.Female)
      forename <- people.Names.forenames(gender)
      surname <- people.Names.surnames
      dateOfBirth <- dateGen(1970,2000)
      email <- people.Names.email(forename, surname, dateOfBirth.toString)
      address <- option(people.Address.ukAddress)
      wtcEligibility <- oneOf(true, false)
      ucEligibility <- oneOf(true, false)
      income <- choose(0, 2 * 120)
    } yield {
      H2SEligibility(
        s"$forename $surname",
        dateOfBirth,
        address,
        email,
        wtcEligibility || (ucEligibility && income > 120),
        wtcEligibility,
        ucEligibility,
        income
      )
    }
  }


  def eligibilityCheck(nino:String) = Action { implicit request =>

    implicit val responseFormatter = Json.format[H2SEligibility]

    UserDetailsGenerator(nino).map { x => 
      Ok(Json.toJson(x))
    }.getOrElse{
      NotFound
    }
  }


}
