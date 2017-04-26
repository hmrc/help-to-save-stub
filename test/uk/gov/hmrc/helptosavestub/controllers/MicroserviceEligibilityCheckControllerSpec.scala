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

import play.api.http.Status
import play.api.libs.json.{JsSuccess, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.helptosavestub.models.EligibilityResult
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class MicroserviceEligibilityCheckControllerSpec extends UnitSpec with WithFakeApplication{

  val fakeRequest = FakeRequest("GET", "/")

  "GET /" should {
    "return a successful EligibilityResult" in {
      val result = MicroserviceEligibilityCheck.eligibilityCheck("QQ123456C")(fakeRequest)
      status(result) shouldBe Status.OK
      val json = contentAsString(result)

      Json.fromJson[EligibilityResult](Json.parse(json)) shouldBe JsSuccess(
        EligibilityResult(Some(MicroserviceEligibilityCheck.user)))
    }
  }


}
