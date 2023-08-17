/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}

class DESThresholdControllerSpec extends TestSupport {

  val fakeRequest = FakeRequest("GET", "/universal-credits/threshold-amount")
    .withHeaders("Authorization" -> "Bearer test")

  val fakeRequestWithIncorrectHeader = FakeRequest("GET", "/universal-credits/threshold-amount")
    .withHeaders("Authorization" -> "Incorrect")

  val controller = new DESThresholdController(testAppConfig, testCC)

  "getThresholdAmount" should {
    "Return Ok status containing json body with the threshold" in {
      val result = controller.getThresholdAmount()(fakeRequest)
      status(result) shouldBe Status.OK

      val json = contentAsString(result)

      json === Json.toJson(controller.thresholdAmount)
    }

    "Return Unauthorized status" in {
      val result = controller.getThresholdAmount()(fakeRequestWithIncorrectHeader)
      status(result) shouldBe Status.UNAUTHORIZED
    }
  }
}
