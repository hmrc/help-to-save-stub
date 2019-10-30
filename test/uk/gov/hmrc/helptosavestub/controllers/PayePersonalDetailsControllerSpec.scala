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
import uk.gov.hmrc.helptosavestub.controllers.TestSupport._
import uk.gov.hmrc.helptosavestub.controllers.support.AkkaMaterializerSpec
import uk.gov.hmrc.smartstub._
import scala.concurrent.ExecutionContext.Implicits.global

class PayePersonalDetailsControllerSpec extends TestSupport with AkkaMaterializerSpec {

  private val fakeRequest = FakeRequest().withHeaders("Authorization" → "Bearer test")

  val payeDetailsController = new PayePersonalDetailsController(actorSystem, testAppConfig, testCC)

  "GET /pay-as-you-earn/02.00.00/individuals/{NINO}" should {

    "returns paye details for a valid NINO" in {
      val nino = randomNINO()
      val result = payeDetailsController.getPayeDetails(nino)(fakeRequest)

      status(result) shouldBe Status.OK
      val json = contentAsString(result)

      json === Json.toJson(payeDetailsController.payeDetails(nino).seeded(nino).getOrElse(sys.error("Could not generate details")))

    }

    "handles 404 cases when supplied NINO cant be found in DES" in {

      val result = payeDetailsController.getPayeDetails(randomNINO().withPrefixReplace("PY404"))(fakeRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "handles 500 cases when there is internal error" in {
      val result = payeDetailsController.getPayeDetails(randomNINO().withPrefixReplace("PY500"))(fakeRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }
}
