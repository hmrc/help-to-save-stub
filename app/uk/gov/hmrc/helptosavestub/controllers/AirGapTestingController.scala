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

import play.api.mvc.{Action, AnyContent, Request, Result}
import play.libs.Json
import uk.gov.hmrc.helptosavefrontend.models.NSIUserInfo
import uk.gov.hmrc.helptosavefrontend.models.NSIUserInfo.ContactDetails
import uk.gov.hmrc.helptosavestub.controllers.UserInfoAPIController.UserInfo
import uk.gov.hmrc.helptosavestub.models.SquidModels
import uk.gov.hmrc.helptosavestub.models.SquidModels.AccountCommand
import uk.gov.hmrc.play.microservice.controller.BaseController

class AirGapTestingController extends BaseController {

  val citizenDetails = CitizenDetailsController.responseGen

  val person = citizenDetails.flatMap(cd ⇒ cd.person.get)
  val address = citizenDetails.flatMap(cd ⇒ cd.address.get)

  val contactDets = ContactDetails(List(address.sample.get.line1.get, address.sample.get.line2.get, address.sample.get.line3.get, "line4", "line5"),
    address.sample.get.postcode.get, address.sample.get.country, "email@gmail.com", Some("01123 123456"), "02")

  val nsiUserInfoAG01 = NSIUserInfo(person.sample.get.firstName.get, person.sample.get.lastName.get, person.sample.get.dateOfBirth.get,
                        "AG010123A", contactDets, "online")



  //method to convert nsiuserinfo to accountcommand
  def convertToAccountCommand(nSIUserInfo: NSIUserInfo): AccountCommand = {
    val newContactDetails = SquidModels.ContactDetails(nSIUserInfo.contactDetails.address(1), nSIUserInfo.contactDetails.address(2),
      Some(nSIUserInfo.contactDetails.address(3)), Some(nSIUserInfo.contactDetails.address(4)), Some(nSIUserInfo.contactDetails.address(5)),
      nSIUserInfo.contactDetails.postCode, nSIUserInfo.contactDetails.countryCode, Some(nSIUserInfo.contactDetails.email), nSIUserInfo.contactDetails.phoneNumber,
      nSIUserInfo.contactDetails.communicationPreference)

    AccountCommand(nSIUserInfo.forename, nSIUserInfo.surname, nSIUserInfo.dateOfBirth.toString, nsiUserInfoAG01.nino,
      newContactDetails, nsiUserInfoAG01.registrationChannel)
  }


  val c = new OAuthController()
  val userInfoAPIController = new UserInfoAPIController


  def token(nino: String): String =
    c.tokenGenerator.seeded(nino).get.access_token


  def userInfo(nino: String): UserInfo =
    userInfoAPIController.userInfoGen.seeded(token(nino)).get

  type Token = String
  val airGapTest1Data: Map[Token,UserInfo] = Map(
    // NINO AG010123C
      "rvvcjuoZatpkmrolydvufvmxphlrceNdsgNHoBiwtoglrqenlkpqlxzakeKpmDizscmqepbaxphxbqvcvotlzff" → userInfo("AG010123C")
  )

  val airGapTest1Json: NSIUserInfo = {
    val userinfo = airGapTest1Data.get("rvvcjuoZatpkmrolydvufvmxphlrceNdsgNHoBiwtoglrqenlkpqlxzakeKpmDizscmqepbaxphxbqvcvotlzff")

  }




//  //drive this nsi user info val through validation
//  def sendAirGapTest(accountCommand: AccountCommand): Unit = {
//    val json = Json.toJson(accountCommand)
//    val request = Request(json)
//
//    airGapTestAG01(Request[Json](json))
//  }
//
//  //plug that into validateCreateAccount
//  def airGapTestAG01(request: Request[AnyContent]): Result = Action { request =>
//      val result = new SquidController().processBody(request)
//  }
//
//


}
