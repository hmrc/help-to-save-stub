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




import play.api.libs.json.Json
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.helptosavestub.controllers.TestSupport._
import uk.gov.hmrc.helptosavestub.controllers.support.AkkaMaterializerSpec
import uk.gov.hmrc.helptosavestub.models.ErrorResponse
import uk.gov.hmrc.helptosavestub.util.{ErrorConversion, INVALID_CORRELATIONID, INVALID_NINO, INVALID_ORIGINATOR_ID, NOT_FOUND_NINO, RESOURCE_NOT_FOUND, SERVER_ERROR}
import uk.gov.hmrc.smartstub._

class IFPayePersonalDetailsControllerSpec extends TestSupport with AkkaMaterializerSpec with ErrorConversion {

  val ifPayeDetailsController = new IFPayePersonalDetailsController(actorSystem, testAppConfig, testCC, mockServicesConfig)
  private val fakeRequest   = FakeRequest().withHeaders("Authorization" -> "Bearer test")

  "GET /if/pay-as-you-earn/02.00.00/individuals/{NINO}" should {

    "returns paye details for a valid NINO" in {
      val nino   = randomNINO()
      val result = ifPayeDetailsController.getPayeDetails(nino)(fakeRequest)

      status(result) shouldBe Status.OK
      val json = contentAsString(result)

      json === Json.toJson(
        ifPayeDetailsController.payeDetails(nino).seeded(nino).getOrElse(sys.error("Could not generate details")))
    }

    "handles 400 cases when supplied NINO is invalid in IF" in {

      val result = ifPayeDetailsController.getPayeDetails(randomNINO().withPrefixReplace("PY400INVALID_NINO"))(fakeRequest)
      val errorResponse =  ErrorResponse(BAD_REQUEST, "INVALID_NINO", "Submission has not passed validation. Invalid parameter nino.")

      toResult(errorResponse) shouldBe   ErrorResponse.errorJson(INVALID_NINO.toString, "Submission has not passed validation. Invalid parameter nino.")
      status(result) shouldBe Status.BAD_REQUEST
    }

    "handles 400 cases when supplied ORIGINATOR_ID is invalid in IF" in {

      val result = ifPayeDetailsController.getPayeDetails(randomNINO().withPrefixReplace("PY400INVALID_ORIGINATOR_ID"))(fakeRequest)

      val errorResponse = ErrorResponse(BAD_REQUEST, "INVALID_ORIGINATOR_ID", "Submission has not passed validation. Invalid header Originator-Id.")

      toResult(errorResponse) shouldBe ErrorResponse.errorJson(INVALID_ORIGINATOR_ID.toString, "Submission has not passed validation. Invalid header Originator-Id.")
      status(result) shouldBe Status.BAD_REQUEST
    }

    "handles 400 cases when supplied CORRELATIONID is invalid in IF" in {
      val result = ifPayeDetailsController.getPayeDetails(randomNINO().withPrefixReplace("PY400INVALID_CORRELATIONID"))(fakeRequest)

      val errorResponse = ErrorResponse(BAD_REQUEST, "INVALID_CORRELATIONID", "Submission has not passed validation. Invalid header CorrelationId.")

      toResult(errorResponse) shouldBe ErrorResponse.errorJson(INVALID_CORRELATIONID.toString, "Submission has not passed validation. Invalid header CorrelationId.")
      status(result) shouldBe Status.BAD_REQUEST
    }

    "handles 404 cases when NINO is not found" in {
      val result = ifPayeDetailsController.getPayeDetails(randomNINO().withPrefixReplace("PY404NOT_FOUND_NINO"))(fakeRequest)

      val errorResponse = ErrorResponse(NOT_FOUND, "NOT_FOUND_NINO", "The remote endpoint has indicated that the nino cannot be found.")

      toResult(errorResponse) shouldBe ErrorResponse.errorJson(NOT_FOUND_NINO.toString, "The remote endpoint has indicated that the nino cannot be found.")
      status(result) shouldBe Status.NOT_FOUND
    }

    "handles 404 cases when PAYE taxpayer details is not found" in {
      val result = ifPayeDetailsController.getPayeDetails(randomNINO().withPrefixReplace("PY404RESOURCE_NOT_FOUND"))(fakeRequest)

      val errorResponse = ErrorResponse(NOT_FOUND, "RESOURCE_NOT_FOUND", "The remote endpoint has indicated that the PAYE taxpayer details not found.")

      toResult(errorResponse) shouldBe ErrorResponse.errorJson(RESOURCE_NOT_FOUND.toString, "The remote endpoint has indicated that the PAYE taxpayer details not found.")
      status(result) shouldBe Status.NOT_FOUND
    }

    "handles 500 cases when IF is experiencing problems" in {
      val result = ifPayeDetailsController.getPayeDetails(randomNINO().withPrefixReplace("PY500SERVER_ERROR"))(fakeRequest)

      val errorResponse = ErrorResponse(INTERNAL_SERVER_ERROR, "SERVER_ERROR", "IF is currently experiencing problems that require live service intervention.")

      toResult(errorResponse) shouldBe ErrorResponse.errorJson(SERVER_ERROR.toString, "IF is currently experiencing problems that require live service intervention.")
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "handles 503 cases when dependent systems are not responding" in {
      val result = ifPayeDetailsController.getPayeDetails(randomNINO().withPrefixReplace("PY503SERVICE_UNAVAILABLE"))(fakeRequest)

      val errorResponse = ErrorResponse(SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "IF is currently experiencing problems that require live service intervention.")

      toResult(errorResponse) shouldBe ErrorResponse.errorJson("SERVICE_UNAVAILABLE", "IF is currently experiencing problems that require live service intervention.")
      status(result) shouldBe Status.SERVICE_UNAVAILABLE
    }

  }
}
