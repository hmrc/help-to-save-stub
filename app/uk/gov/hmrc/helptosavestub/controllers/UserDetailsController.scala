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
import org.scalacheck.Gen._
import play.api.libs.json.{Format, Json}
import play.api.mvc.Action
import hmrc.smartstub._
import uk.gov.hmrc.helptosavestub.controllers.UserDetailsController.UserDetails
import uk.gov.hmrc.helptosavestub.util.DummyData.UserInfo
import uk.gov.hmrc.helptosavestub.util.{DummyData, Logging}
import uk.gov.hmrc.play.microservice.controller.BaseController

class UserDetailsController extends BaseController with Logging {

  type NINO = String

  val userDetailsGen: Gen[UserDetails] =
    for {
      fname ← Gen.forename()
      sname ← Gen.some(Gen.surname)
      dob ← Gen.some(Gen.date(1940, 2017))
      email ← some("email@email.com")
    } yield UserDetails(fname, sname, email, dob)


  implicit val ninoEnum: Enumerable[String] = pattern"ZZ999999Z"

  def retrieveDetails(nino: NINO) = Action { implicit request =>
    logger.info(s"Received request to get user details for nino $nino")
    DummyData.find(nino).map(toUserDetails).orElse(userDetailsGen.seeded(nino)).map { response =>
      logger.info(s"Responding to request from $nino with details $response")
      Ok(Json.toJson(response))
    }.getOrElse{
      logger.warn(s"Could not find or generate data fro nino $nino")
      NotFound
    }
  }

  def toUserDetails(userInfo: UserInfo) = UserDetails(
    userInfo.forename, userInfo.surname, userInfo.email, userInfo.dateOfBirth
  )
}

object UserDetailsController{
  case class UserDetails(name: String,
                         lastName: Option[String],
                         email: Option[String],
                         dateOfBirth: Option[LocalDate])

  object UserDetails {
    implicit val userDetailFormat: Format[UserDetails] = Json.format[UserDetails]
  }
}
