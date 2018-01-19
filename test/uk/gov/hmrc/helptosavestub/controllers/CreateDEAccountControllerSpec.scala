/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.helptosavestub.models.NSIUserInfo
import uk.gov.hmrc.helptosavestub.models.NSIUserInfo.ContactDetails
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CreateDEAccountControllerSpec extends UnitSpec with WithFakeApplication {

  val testCreateAccount = NSIUserInfo(
    "Donald", "Duck", LocalDate.of(1990, 1, 1), "AA999999A", // scalastyle:ignore magic.number
                      ContactDetails("1", ",Test Street 2", None, None, None, "BN124XH", Some("GB"), None, None, "00"),
    "callCentre")

  val controller = CreateDEAccountController

  "The CreateDEAccountController" when {
    "creating a DE account" must {
      "return 201 for valid requests" in {

        val request = FakeRequest()
          .withJsonBody(Json.toJson(testCreateAccount))

        val result = controller.createDEAccount()(request)

        status(result) shouldBe Status.CREATED
      }

      "return a 400 for a request with no json in it" in {
        val request = FakeRequest()
        val result = controller.createDEAccount()(request)
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return a 400 for a request with invalid registrationChannel" in {
        val request = FakeRequest()
          .withJsonBody(Json.toJson(testCreateAccount.copy(registrationChannel = "online")))
        val result = controller.createDEAccount()(request)
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return a 400 for a request with invalid communicationPreference" in {
        val request = FakeRequest()
          .withJsonBody(Json.toJson(testCreateAccount.copy(contactDetails = testCreateAccount.contactDetails.copy(communicationPreference = "02"))))
        val result = controller.createDEAccount()(request)
        status(result) shouldBe Status.BAD_REQUEST
      }
    }
  }

}
