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

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64

import play.api.libs.json.{Json, _}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.helptosavestub.controllers.NSIGetAccountBehaviour.NSIGetAccountByNinoResponse
import uk.gov.hmrc.helptosavestub.controllers.NSIGetTransactionsBehaviour.NSIGetTransactionsByNinoResponse
import uk.gov.hmrc.helptosavestub.models.NSIUserInfo
import uk.gov.hmrc.helptosavestub.controllers.support.AkkaMaterializerSpec
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.mvc.Result

class NSIControllerSpec extends UnitSpec with WithFakeApplication with AkkaMaterializerSpec {

  import NSIUserInfo._

  val testCreateAccount = NSIUserInfo(
    "Donald", "Duck", LocalDate.of(1990, 1, 1), "AA999999A", // scalastyle:ignore magic.number
                      ContactDetails("1", ",Test Street 2", None, None, None, "BN124XH", Some("GB"), Some("dduck@email.com"), None, "02"),
    "online")

  val authHeader = {
    val encoded = new String(Base64.getEncoder().encode("username:password".getBytes(StandardCharsets.UTF_8)))
    "Authorization-test" → s"Basic: $encoded"
  }

  def errorMessageIds(result: Result): List[String] = (jsonBodyOf(result) \ "errors").as[Seq[String]](Reads.seq((__ \ "errorMessageId").read[String])).toList

  def correlationIds(result: Result): String = (jsonBodyOf(result) \ "correlationId").as[String]

  "Post /nsi-services/account  " should {
    "return a successful Create Account" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
        .withJsonBody(Json.toJson(testCreateAccount))

      val result = NSIController.createAccount()(request)
      status(result) shouldBe CREATED
    }

    "return a 401  UNAUTHORIZED" in {
      val request = FakeRequest()
        .withJsonBody(Json.toJson(testCreateAccount))
      val result = NSIController.createAccount()(request)
      status(result) shouldBe UNAUTHORIZED
    }

    "return a 400 for a bad request in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.createAccount()(request)
      status(result) shouldBe BAD_REQUEST
    }
  }

  "Put /create-account  " should {
    "return a successful successful status" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
        .withJsonBody(Json.toJson(testCreateAccount))

      val result = NSIController.updateEmailOrHealthCheck()(request)
      status(result) shouldBe OK
    }

    "return a 401  UNAUTHORIZED" in {
      val request = FakeRequest()
        .withJsonBody(Json.toJson(testCreateAccount))
      val result = NSIController.updateEmailOrHealthCheck()(request)
      status(result) shouldBe UNAUTHORIZED
    }

    "return a 400 for a bad request in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.updateEmailOrHealthCheck()(request)
      status(result) shouldBe BAD_REQUEST
    }
  }

  "Get /nsi-services/account" should {
    "return a successful status when given an existing nino" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getAccount(Some("correlationId"), Some("EM000001A"), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe OK
      val json = contentAsString(result)
      Json.fromJson[NSIGetAccountByNinoResponse](Json.parse(json)).get shouldBe NSIGetAccountByNinoResponse.bethNSIResponse(Some("correlationId"))
    }

    "return air gap test data provided by ATOS" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getAccount(correlationId = None, nino = Some("NB123533B"), version = Some("V1.0"), systemId = Some("systemId"))(request)
      status(result) shouldBe OK
      val json = contentAsJson(result)
      // should return the data provided by ATOS unmodified so we know we're testing what they sent, not our stub's logic
      (json \ "correlationId").as[String] shouldBe "551485a3-001d-91e8-060e-890c40505bd7"
      (json \ "currentInvestmentMonth" \ "investmentRemaining").as[String] shouldBe "50.00"
    }

    "return a 400 with errorMessageId HTS-API015-002 when the service version is missing" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EM000001A"), None, Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-002")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-003 when given an unsupported service version" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EM000001A"), Some("V1.5"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-003")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-004 when not given a nino" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), None, Some("V1.0"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-004")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-005 when given a nino in the incorrect format" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EZ00000A"), Some("V1.0"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-005")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-006 when given a nino not found" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EZ000001A"), Some("V1.0"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-006")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 500 when given a nino with 500 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getAccount(Some("correlationId"), Some("EM500001A"), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a 401 when given a nino with 401 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getAccount(Some("correlationId"), Some("EM000401A"), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe UNAUTHORIZED
    }

    "return a 400 with errorMessageId HTS-API015-012 when systemId is not present" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EZ000001A"), Some("V1.0"), None)(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-012")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with two error responses when systemId is not present and the nino is in the incorrect format" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EZ00000A"), Some("V1.0"), None)(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-005", "HTS-API015-012")
      correlationIds(result) shouldBe "correlationId"
    }
  }

  "Get /nsi-services/transactions" should {
    "return a successful status when given an existing nino" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getTransactions(Some("correlationId"), Some("EM000001A"), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe OK
      val json = contentAsString(result)
      Json.fromJson[NSIGetTransactionsByNinoResponse](Json.parse(json)).get shouldBe NSIGetTransactionsByNinoResponse.bethResponse(Some("correlationId"))
    }

    "return a 400 with errorMessageId HTS-API015-002 when the service version is missing" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some("EM000001A"), None, Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-002")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-003 when given an unsupported service version" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some("EM000001A"), Some("V1.5"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-003")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-004 when not given a nino" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), None, Some("V1.0"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-004")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-005 when given a nino in the incorrect format" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some("EZ00000A"), Some("V1.0"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-005")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-006 when given a nino not found" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some("EZ000001A"), Some("V1.0"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-006")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 500 when given a nino with 500 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getTransactions(Some("correlationId"), Some("EM500001A"), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a 401 when given a nino with 401 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getTransactions(Some("correlationId"), Some("EM000401A"), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe UNAUTHORIZED
    }

    "return a 400 with errorMessageId HTS-API015-012 when systemId is not present" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some("EZ000001A"), Some("V1.0"), None)(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-012")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with two error responses when systemId is not present and the nino is in the incorrect format" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some("EZ00000A"), Some("V1.0"), None)(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-005", "HTS-API015-012")
      correlationIds(result) shouldBe "correlationId"
    }
  }
}
