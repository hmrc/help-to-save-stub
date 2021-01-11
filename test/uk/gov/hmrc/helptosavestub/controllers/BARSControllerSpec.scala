/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.helptosavestub.controllers.support.AkkaMaterializerSpec

class BARSControllerSpec extends TestSupport with AkkaMaterializerSpec {

  val controller = new BARSController(testCC)

  "validateBankDetails" should {
    "return accountNumberWithSortCodeIsValid as true when the given sort code and account number are in the correct format" in {
      val fakeRequest = FakeRequest("POST", "/").withJsonBody(Json.parse("""{
          |  "account": {
          |    "sortCode": "123456",
          |    "accountNumber": "12345678"
          |  }
          |}""".stripMargin))

      val result = await(controller.validateBankDetails(fakeRequest))

      status(result) shouldBe 200
      jsonBodyOf(result).toString shouldBe
        """{"accountNumberWithSortCodeIsValid":true,"sortCodeIsPresentOnEISCD":"yes"}""".stripMargin
    }

    "return accountNumberWithSortCodeIsValid as false when the given sort code is in the incorrect format" in {
      val fakeRequest = FakeRequest("POST", "/").withJsonBody(Json.parse("""{
           |  "account": {
           |    "sortCode": "12-34-56",
           |    "accountNumber": "12345678"
           |  }
           |}""".stripMargin))

      val result = await(controller.validateBankDetails(fakeRequest))

      status(result) shouldBe 200
      jsonBodyOf(result).toString shouldBe
        """{"accountNumberWithSortCodeIsValid":false,"sortCodeIsPresentOnEISCD":"yes"}""".stripMargin
    }

    "return accountNumberWithSortCodeIsValid as false when the given account number is in the incorrect format" in {
      val fakeRequest = FakeRequest("POST", "/").withJsonBody(Json.parse("""{
          |  "account": {
          |    "sortCode": "123456",
          |    "accountNumber": "123-45678"
          |  }
          |}""".stripMargin))

      val result = await(controller.validateBankDetails(fakeRequest))

      status(result) shouldBe 200
      jsonBodyOf(result).toString shouldBe
        """{"accountNumberWithSortCodeIsValid":false,"sortCodeIsPresentOnEISCD":"yes"}""".stripMargin
    }
  }

}
