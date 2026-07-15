/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckHipController.NewTaxCreditStatus.ninoNotFound
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckHipController.WorkingTaxCreditEntitlement.awardIncludesWTC
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckHipController.{EligibilityCheckHipRequest, EligibilityCheckHipResult}
import uk.gov.hmrc.helptosavestub.controllers.TestSupport.StringOps
import uk.gov.hmrc.helptosavestub.controllers.support.AkkaMaterializerSpec

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, TimeoutException}

class EligibilityCheckHipControllerSpec extends TestSupport with AkkaMaterializerSpec {


  val hipHeaders: String = appConfig.hipHeaders
  val correlationId: String = UUID.randomUUID().toString
  val goUKOriginatorId: String = appConfig.goUKOriginatorId


  def fakeRequest (ucClaimant: Option[Boolean], withinThreshold: Option[Boolean]): FakeRequest[AnyContentAsJson] = {
    val request = EligibilityCheckHipRequest(ninoNotFound, awardIncludesWTC, 0, 0, ucClaimant, withinThreshold)

    val requestJson = Json.toJson(request)
    FakeRequest("POST", "/")
      .withHeaders("Authorization" -> hipHeaders)
      .withHeaders("correlationId" -> correlationId)
      .withHeaders("gov-uk-originator-id" -> goUKOriginatorId)
      .withJsonBody(requestJson)
  }

  def fakeBadRequest(creditStatus: String, entitlement: String ): FakeRequest[AnyContentAsJson] = {
    val request = "{" +
      s" \"newTaxCreditStatus\" : \"$creditStatus\"," +
      s"  \"workingTaxCreditEntitlement\" : \"$entitlement\"," +
      "  \"workingTaxCreditTaperedHouseholdAward\" : 0," +
      "  \"childTaxCreditTaperedHouseholdAward\" : 0" +
      "}"
    FakeRequest("POST", "/")
      .withHeaders("Authorization" -> hipHeaders)
      .withHeaders("correlationId" -> correlationId)
      .withHeaders("gov-uk-originator-id" -> goUKOriginatorId)
      .withJsonBody(Json.parse(request))
  }

  val eligCheckController = new EligibilityCheckHipController(actorSystem, testCC)
  val badReqErrorResponse: String = "{\"origin\" : \"Hip\"," +
    "\"response\" : " +
    "{\"failures\" : [ {" +
    " \"reason\" : \"Constraint Violation - Invalid/Missing header\"," +
    " \"code\" : \"400.1\"} ] }}"

  val badReqErrorResponse1: String = "{\"origin\" : \"Hip\"," +
    "\"response\" : " +
    "{\"failures\" : [ {" +
    " \"reason\" : \"Constraint Violation - Invalid/Missing input parameter\"," +
    " \"code\" : \"400.1\"} ] }}"

  val badReqErrorResponse2: String = "{\"origin\" : \"Hip\"," +
    "\"response\" : " +
    "{\"failures\" : [ {" +
    " \"reason\" : \"HTTP message not readable\"," +
    " \"code\" : \"400.2\"} ] }}"


  "POST /" should {

    "verify unauthorised request " in {
      val result = eligCheckController.eligibilityCheck(randomNINO())(FakeRequest("POST", "/").withHeaders("Authorization" -> "Basic xyz"))
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "verify Invalid originator ID in request " in {
      val result = eligCheckController.eligibilityCheck(randomNINO())(FakeRequest("POST", "/")
        .withHeaders("Authorization" -> hipHeaders)
        .withHeaders("correlationId" -> correlationId)
        .withHeaders("gov-uk-originator-id" -> "abc"))
      status(result) shouldBe Status.BAD_REQUEST
      contentAsJson(result) shouldBe Json.parse(badReqErrorResponse)

    }

    "verify correlationId is passed in request " in {
      val result = eligCheckController.eligibilityCheck(randomNINO())(FakeRequest("POST", "/")
        .withHeaders("Authorization" -> hipHeaders)
        .withHeaders("gov-uk-originator-id" -> goUKOriginatorId))
      status(result) shouldBe Status.BAD_REQUEST
      contentAsJson(result) shouldBe Json.parse(badReqErrorResponse)
    }

    "verify goUKOriginatorId is passed in request " in {
      val result = eligCheckController.eligibilityCheck(randomNINO())(FakeRequest("POST", "/")
        .withHeaders("Authorization" -> hipHeaders)
        .withHeaders("correlationId" -> correlationId))
      status(result) shouldBe Status.BAD_REQUEST
      contentAsJson(result) shouldBe Json.parse(badReqErrorResponse)
    }

    "verify invalid nino" in {
      val result = eligCheckController.eligibilityCheck("1234")(fakeRequest(Some(true), Some(true)))
      status(result) shouldBe Status.BAD_REQUEST
      contentAsJson(result) shouldBe Json.parse(badReqErrorResponse1)
    }


    "verify invalid credit status" in {
      val result = eligCheckController.eligibilityCheck(randomNINO())(fakeBadRequest("NINO not currently in an NTC current year award", "abc"))
      status(result) shouldBe Status.BAD_REQUEST
      contentAsJson(result) shouldBe Json.parse(badReqErrorResponse2)
    }

    "verify invalid entitlement" in {
      val result = eligCheckController.eligibilityCheck(randomNINO())(fakeBadRequest("xyz", "Current Award includes a WTC entitlement"))
      status(result) shouldBe Status.BAD_REQUEST
      contentAsJson(result) shouldBe Json.parse(badReqErrorResponse2)
    }

    "returns true when user is eligible" in {
      verifyEligibility(randomNINO().withPrefixReplace("EL07"), "CUSTOMER ELIGIBLE FOR HTS ACCOUNT", "ENTITLED TO WTC AND RECEIVE POSITIVE TAX CREDIT")
    }

    "returns false when user is not eligible for reason code 2" in {
      verifyEligibility(randomNINO().withPrefixReplace("WP99101"), "UNKNOWN ELIGIBILITY BECAUSE CALL TO DWP FAILED", "NOT ENTITLED TO WTC AND UC NOT CHECKED")
    }

    "returns false when user is not eligible for reason code 3" in {
      verifyEligibility(randomNINO().withPrefixReplace("WP00101"), "CUSTOMER INELIGIBLE FOR HTS ACCOUNT", "ENTITLED TO WTC BUT NOT IN RECEIPT OF POSITIVE TAX CREDIT AND NOT IN RECEIPT DWP UC", Status.OK,Some(false), None)
    }

    "returns false when user is not eligible for reason code 10" in {
      verifyEligibility(randomNINO().withPrefixReplace("DS01"), "CUSTOMER INELIGIBLE FOR HTS ACCOUNT", "MANUAL")
    }

    "returns false when user already has an account" in {
      verifyEligibility(randomNINO().withPrefixReplace("AC"), "CUSTOMER INELIGIBLE FOR HTS ACCOUNT", "HTS ACCOUNT HELD ALREADY")
    }
    "returns true with ENTITLED TO WTC AND RECEIVE POSITIVE TAX CREDIT" in {
      intercept[TimeoutException] {
        Await.ready(eligCheckController.eligibilityCheck(randomNINO().withPrefixReplace("TM02"))(fakeRequest(Some(false), Some(false))), 1.second)
      }
    }

    "returns 404 for nino starting with WP1144" in {
      verifyEligibility(randomNINO().withPrefixReplace("WP1144"), "CUSTOMER INELIGIBLE FOR HTS ACCOUNT", "HTS ACCOUNT HELD ALREADY", 404)
    }

    def verifyEligibility(
                           nino: String,
                           expectedResult: String,
                           expectedReason: String,
                           expectedStatus: Int = Status.OK,
                           ucClaimant: Option[Boolean]      = None,
                           withinThreshold: Option[Boolean] = None): Unit = {

      val result = eligCheckController.eligibilityCheck(nino)(fakeRequest(ucClaimant, withinThreshold))
      status(result) shouldBe expectedStatus
      val json = contentAsString(result)
      if( status(result) == Status.OK){
        val eligibilityCheckHipResult = Json.fromJson[EligibilityCheckHipResult](Json.parse(json)).get
        eligibilityCheckHipResult.eligibilityResult shouldBe expectedResult
        eligibilityCheckHipResult.eligibilityReason shouldBe expectedReason
      }
    }
  }
}
