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
import uk.gov.hmrc.helptosavestub.models.{ContactPreference, EligibilityResult, UserDetails}
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import org.scalacheck._
import hmrc.smartstub._
//import hmrc.smartstub.Enumerable.instances.ninoEnum

object MicroserviceEligibilityCheck extends MicroserviceEligibilityCheck

trait MicroserviceEligibilityCheck extends BaseController {

  implicit val ninoEnum: Enumerable[String] = pattern"ZZ999999Z"

  /**
    * This generator defines the randomly created UserDetails records from a NINO
    */
  val generator = for {
    forename <- Gen.forename
    surname <- Gen.surname
    dateOfBirth <- Gen.date(1970,2000)
    email = s"${forename.toLowerCase}.${surname.toLowerCase}@gmail.com"
    phoneNumber <- Gen.ukPhoneNumber
    address <- Gen.ukAddress
    contactPreference <- Gen.oneOf(ContactPreference.SMS, ContactPreference.Email)
  } yield {
    UserDetails(
      s"$forename $surname",
      "",
      dateOfBirth,
      email,
      phoneNumber,
      address,
      contactPreference
    )
  }

  def user = UserDetails(
    "Bob Bobber",
    "QQ123456C",
    LocalDate.now,
    "bob@email.com",
    "0879371657",
    List("Happy land","happy street"),
    ContactPreference.Email
  )

  val store = generator.asMutable[String]
  store("QQ123456C") = user

  def eligibilityCheck(ninoIn:String) = Action { implicit request =>
    store.get(ninoIn).map { x => 
      Ok(Json.toJson(x.copy(nino = ninoIn)))
    }.getOrElse{
      NotFound
    }
  }

}
