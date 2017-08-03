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
import java.util.Base64

import org.scalacheck.Gen
import play.api.libs.json.{Format, Json}
import play.api.mvc.Action
import hmrc.smartstub._
import uk.gov.hmrc.helptosavestub.controllers.UserDetailsController.UserDetails
import uk.gov.hmrc.helptosavestub.util.DummyData
import uk.gov.hmrc.play.microservice.controller.BaseController

class UserDetailsController extends BaseController {

  type NINO = String

  val userDetailsGen: Gen[UserDetails] = {
    import Gen._

    val userDetails = for {
      fname ← Gen.some(Gen.forename())
      mname ← Gen.some(Gen.forename())
      sname ← Gen.some(Gen.surname)
      dob ← Gen.some(Gen.date(1940, 2017))
      address ← Gen.some(Gen.ukAddress.map{_.map{x ⇒ some(const(x))}}.toString())
      postcode0 ← Gen.some(Gen.postcode)
      cname0 ← some(const("United Kingdom"))
      ccode0 ← some(const("GB"))
      email0 ← None
    } yield UserDetails(fname, mname, sname, dob, address, postcode0, cname0, ccode0, email0)

    for {
      userDetails0 ← userDetails
    } yield userDetails0.get
  }


  def retrieveDetails(nino: NINO) = Action { implicit request =>
    implicit val ninoEnum: Enumerable[String] = pattern"ZZ999999Z"
    DummyData.hardCodedData.get(encode(nino)).orElse(userDetailsGen.seeded(nino)).map { response =>
      Ok(Json.toJson(response))
    }.getOrElse{
      NotFound
    }
  }

  def encode(nino: String): String = new String(Base64.getEncoder.encode(nino.getBytes()))

}


object UserDetailsController{
  case class UserDetails(
                          givenName: Option[String],
                          middleName: Option[String],
                          familyName: Option[String],
                          birthdate: Option[LocalDate],
                          formattedAddress: Option[String],
                          postCode: Option[String],
                          countryName: Option[String],
                          countryCode: Option[String],
                          email: Option[String]
                        )

  object UserDetails {
    implicit val userDetailFormat: Format[UserDetails] = Json.format[UserDetails]
  }
}
