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
import uk.gov.hmrc.play.microservice.controller.BaseController
import org.scalacheck.Gen
import hmrc.smartstub._

object ItmpController extends BaseController {

  implicit val ninoEnum: Enumerable[String] = pattern"ZZ999999Z"

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

  def generator(in: String) = for {
    forename <- Gen.forename
    surname <- Gen.surname
    dateOfBirth <- Gen.date(1970,2000)
    email = s"${forename.toLowerCase}.${surname.toLowerCase}@gmail.com"
    address <- Gen.option(Gen.ukAddress)
    wtcEligibility <- Gen.oneOf(true, false)
    ucEligibility <- Gen.oneOf(true, false)
    income <- Gen.choose(0, 2 * 120)
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

  def eligibilityCheck(nino:String) = Action { implicit request =>
    implicit val responseFormatter = Json.format[H2SEligibility]

    generator(nino).seeded(nino).map { x =>
      Ok(Json.toJson(x))
    }.getOrElse{
      NotFound
    }
  }
}
