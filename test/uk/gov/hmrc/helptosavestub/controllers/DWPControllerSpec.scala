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

import java.util.UUID

import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.helptosavestub.controllers.DWPController.UCDetails
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._

import scala.concurrent.Future

class DWPControllerSpec extends UnitSpec with WithFakeApplication {

  val fakeRequest = FakeRequest().withHeaders("Authorization" → "Bearer test")

  val dwpController = new DWPController

  val wp01Json = UCDetails("Y", Some("Y"))
  val wp02Json = UCDetails("Y", Some("N"))
  val wp03Json = UCDetails("N", None)

  val systemId = "607"
  val threshold = 650
  val newUUID = UUID.randomUUID()

  "dwpEligibilityCheck" must {

    "return a 400 status with no json payload when given a nino starting with WS400" in {
      val result: Future[Result] = dwpController.dwpClaimantCheck("WS400123A", systemId, threshold, Some(newUUID))(fakeRequest)
      status(result) shouldBe Status.BAD_REQUEST
    }

    "return a 404 status with no json payload when given a nino starting with WS404" in {
      val result: Future[Result] = dwpController.dwpClaimantCheck("WS404123A", systemId, threshold, Some(newUUID))(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }

    "return a 500 status with no json payload when given a nino starting with WS500" in {
      val result: Future[Result] = dwpController.dwpClaimantCheck("WS500123A", systemId, threshold, Some(newUUID))(fakeRequest)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return a 504 status with no json payload when given a nino starting with WS504" in {
      val result: Future[Result] = dwpController.dwpClaimantCheck("WS504123A", systemId, threshold, Some(newUUID))(fakeRequest)
      status(result) shouldBe Status.GATEWAY_TIMEOUT
    }

  }

}