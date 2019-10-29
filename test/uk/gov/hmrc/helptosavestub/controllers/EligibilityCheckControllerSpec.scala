/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckController.EligibilityCheckResult
import uk.gov.hmrc.helptosavestub.controllers.TestSupport._
import uk.gov.hmrc.helptosavestub.controllers.support.AkkaMaterializerSpec
import scala.concurrent.ExecutionContext.Implicits.global

class EligibilityCheckControllerSpec extends TestSupport with AkkaMaterializerSpec {

  val fakeRequest = FakeRequest("GET", "/").withHeaders("Authorization" → "Bearer test")

  val eligCheckController = new EligibilityCheckController(actorSystem, testAppConfig, testCC)

  "GET /" should {

    "returns true when user is eligible" in {
      verifyEligibility(randomNINO().withPrefixReplace("EL07"), 1)
    }

    "returns false when user is not eligible for reason code 2" in {
      verifyEligibility(randomNINO().withPrefixReplace("WP99101"), 4)
    }

    "returns false when user is not eligible for reason code 3" in {
      verifyEligibility(randomNINO().withPrefixReplace("WP00101"), 2, Some("N"), None)
    }

    "returns false when user already has an account" in {
      verifyEligibility(randomNINO().withPrefixReplace("AC"), 3)
    }

      def verifyEligibility(nino:            String,
                            resultCode:      Int,
                            ucClaimant:      Option[String] = None,
                            withinThreshold: Option[String] = None): Unit = {

        val result = eligCheckController.eligibilityCheck(nino, ucClaimant, withinThreshold)(fakeRequest)
        status(result) shouldBe Status.OK
        val json = contentAsString(result)
        val expected = resultCode match {
          case 1 ⇒ "Eligible to HtS Account"
          case 2 ⇒ "Ineligible to HtS Account"
          case 3 ⇒ "HtS account already exists"
          case 4 ⇒ "Unknown eligibility because call to DWP failed"
          case _ ⇒ sys.error("Invalid result code")
        }

        Json.fromJson[EligibilityCheckResult](Json.parse(json)).get.result shouldBe expected

      }
  }
}
