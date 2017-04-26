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
import smartstub._

object MicroserviceEligibilityCheck extends MicroserviceEligibilityCheck

trait MicroserviceEligibilityCheck extends BaseController {

  /**
    * This generator defines the randomly created UserDetails records from a NINO
    */
  object UserDetailsGenerator extends SmartStubGenerator[String, UserDetails] {
    import org.scalacheck.Gen._

    def from(in: String): Option[Long] = fromNino(in)

    def generator(in: String) = for {
      gender <- oneOf(people.Male, people.Female)
      forename <- people.Names.forenames(gender)
      surname <- people.Names.surnames
      dateOfBirth <- dateGen(1970,2000)
      email <- people.Names.email(forename, surname, dateOfBirth.toString)
      phoneNumber <- people.Names.phoneNo
      address <- people.Address.ukAddress
      contactPreference <- oneOf(ContactPreference.SMS, ContactPreference.Email)
    } yield {
      UserDetails(
        s"$forename $surname",
        in.filter(_.isLetterOrDigit).toUpperCase,
        dateOfBirth,
        email,
        phoneNumber,
        address,
        contactPreference
      )
    }

    // Lets hard-code a specific NINO
    state("QQ123456C") = user
  }

  val user = UserDetails(
    "Bob Bobber",
    "QQ123456C",
    LocalDate.now(),
    "bob@email.com",
    "0879371657",
    List("Happy land","happy street"),
    ContactPreference.Email
  )

  def eligibilityCheck(nino:String) = Action { implicit request =>
    UserDetailsGenerator(nino).map { x => 
      Ok(Json.toJson(x))
    }.getOrElse{
      NotFound
    }
  }

}
