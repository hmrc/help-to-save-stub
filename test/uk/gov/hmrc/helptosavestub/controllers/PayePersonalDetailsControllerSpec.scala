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
import play.api.test.Helpers._
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.controllers.TestSupport._
import uk.gov.hmrc.helptosavestub.controllers.support.AkkaMaterializerSpec
import uk.gov.hmrc.smartstub._

class PayePersonalDetailsControllerSpec extends TestSupport with AkkaMaterializerSpec {
  override implicit lazy val appConfig: AppConfig = testAppConfig
  val payeDetailsController = new PayePersonalDetailsController(actorSystem, testCC)
  private val fakeRequestBearerTokenIF   = FakeRequest().withHeaders("Authorization" -> "Bearer test-if")
  private val fakeRequestBearerTokenDES   = FakeRequest().withHeaders("Authorization" -> "Bearer test-des")

  "GET /pay-as-you-earn/02.00.00/individuals/{NINO}" should {

    "returns paye details for a valid NINO" in {
      val nino   = randomNINO()
      val result = payeDetailsController.getDESPayeDetails(nino)(fakeRequestBearerTokenDES)

      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.parse(payeDetailsController.payeDetails(nino).seeded(nino).getOrElse(fail()))
    }

    "handles 404 cases when supplied NINO cant be found in DES" in {

      val result = payeDetailsController.getDESPayeDetails(randomNINO().withPrefixReplace("PY404"))(fakeRequestBearerTokenDES)

      status(result) shouldBe Status.NOT_FOUND
    }

    "handles 500 cases when there is internal error in DES" in {
      val result = payeDetailsController.getDESPayeDetails(randomNINO().withPrefixReplace("PY500"))(fakeRequestBearerTokenDES)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "handles non-standard error code cases when there is internal error in DES" in {
      val result = payeDetailsController.getDESPayeDetails(randomNINO().withPrefixReplace("PY924"))(fakeRequestBearerTokenDES)

      status(result) shouldBe 924
    }
  }

  "GET /if/pay-as-you-earn/02.00.00/individuals/{NINO}" should {

    "returns paye details for a valid NINO" in {
      val nino = randomNINO()
      val result = payeDetailsController.getIFPayeDetails(nino)(fakeRequestBearerTokenIF)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.parse(payeDetailsController.payeDetails(nino).seeded(nino).getOrElse(fail()))
    }


    "handles 404 cases when supplied NINO cant be found in IF" in {

      val result = payeDetailsController.getIFPayeDetails(randomNINO().withPrefixReplace("PY404"))(fakeRequestBearerTokenIF)

      status(result) shouldBe Status.NOT_FOUND
    }

    "handles 500 cases when there is internal error in IF" in {
      val result = payeDetailsController.getIFPayeDetails(randomNINO().withPrefixReplace("PY500"))(fakeRequestBearerTokenIF)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "handles non-standard error code cases when there is internal error in IF" in {
      val result = payeDetailsController.getIFPayeDetails(randomNINO().withPrefixReplace("PY924"))(fakeRequestBearerTokenIF)

      status(result) shouldBe 924
    }
  }
}
