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
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.helptosavestub.controllers.NSIGetAccountBehaviour.NSIGetAccountByNinoResponse
import uk.gov.hmrc.helptosavestub.controllers.NSIGetTransactionsBehaviour.NSIGetTransactionsByNinoResponse
import uk.gov.hmrc.helptosavestub.controllers.TestSupport._
import uk.gov.hmrc.helptosavestub.controllers.support.AkkaMaterializerSpec
import uk.gov.hmrc.helptosavestub.models.NSIPayload

class NSIControllerSpec extends TestSupport with AkkaMaterializerSpec {

  val generator = new Generator(1)
  val ninoWithAccount: String = randomNINO().withPrefixReplace("EM0").withSuffixReplace("001A")
  val ninoContaining500: String = randomNINO().withPrefixReplace("EM500")
  val ninoContaining401: String = randomNINO().withPrefixReplace("EM").withSuffixReplace("401A")

  import NSIPayload._

  val testCreateAccount = NSIUserInfo(
    "Donald", "Duck", LocalDate.of(1990, 1, 1), generator.nextNino.nino, // scalastyle:ignore magic.number
                      ContactDetails("1", ",Test Street 2", None, None, None, "BN124XH", Some("GB"), Some("dduck@email.com"), None, "02"),
    "online")

  val (authHeader, authHeaderDifferentCase) = {
    val encoded = new String(Base64.getEncoder.encode("username:password".getBytes(StandardCharsets.UTF_8)))
    val headerValue = s"Basic: $encoded"
    (
      "Authorization-test" → headerValue,
      "aUtHoRiZaTiOn-test" → headerValue
    )
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

    "treat headers names case insensitively as per RFC 2616" in {
      val request = FakeRequest()
        .withHeaders(authHeaderDifferentCase)
        .withJsonBody(Json.toJson(testCreateAccount))

      val result = NSIController.createAccount()(request)
      status(result) shouldBe CREATED
    }

    "return a 401 UNAUTHORIZED when request does not have the correct auth header is" in {
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
      val result = NSIController.getAccount(Some("correlationId"), Some(ninoWithAccount), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe OK
      val json = contentAsString(result)
      Json.fromJson[NSIGetAccountByNinoResponse](Json.parse(json)).get shouldBe NSIGetAccountByNinoResponse.bethNSIResponse(Some("correlationId"))
    }

    "return a 400 with errorMessageId HTS-API015-002 when the service version is missing" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some(ninoWithAccount), None, Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-002")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-003 when given an unsupported service version" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some(ninoWithAccount), Some("V1.5"), Some("systemId"))(request))
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
      val result = await(NSIController.getAccount(Some("correlationId"), Some("not-a-nino"), Some("V1.0"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-005")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-006 when given a nino not found" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some(generator.nextNino.nino), Some("V1.0"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-006")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 500 when given a nino with 500 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getAccount(Some("correlationId"), Some(ninoContaining500), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a 401 when given a nino with 401 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getAccount(Some("correlationId"), Some(ninoContaining401), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe UNAUTHORIZED
    }

    "return a 400 with errorMessageId HTS-API015-012 when systemId is not present" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some(ninoWithAccount), Some("V1.0"), None)(request))
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
      val result = NSIController.getTransactions(Some("correlationId"), Some(ninoWithAccount), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe OK
      val json = contentAsString(result)
      Json.fromJson[NSIGetTransactionsByNinoResponse](Json.parse(json)).get shouldBe NSIGetTransactionsByNinoResponse.bethResponse(Some("correlationId"))
    }

    "return a 400 with errorMessageId HTS-API015-002 when the service version is missing" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some(ninoWithAccount), None, Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-002")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-003 when given an unsupported service version" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some(ninoWithAccount), Some("V1.5"), Some("systemId"))(request))
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
      val result = await(NSIController.getTransactions(Some("correlationId"), Some(generator.nextNino.nino), Some("V1.0"), Some("systemId"))(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-006")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 500 when given a nino with 500 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getTransactions(Some("correlationId"), Some(ninoContaining500), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a 401 when given a nino with 401 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getTransactions(Some("correlationId"), Some(ninoContaining401), Some("V1.0"), Some("systemId"))(request)
      status(result) shouldBe UNAUTHORIZED
    }

    "return a 400 with errorMessageId HTS-API015-012 when systemId is not present" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some(generator.nextNino.nino), Some("V1.0"), None)(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-012")
      correlationIds(result) shouldBe "correlationId"
    }

    "return a 400 with two error responses when systemId is not present and the nino is in the incorrect format" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getTransactions(Some("correlationId"), Some("not-a-nino"), Some("V1.0"), None)(request))
      status(result) shouldBe BAD_REQUEST
      errorMessageIds(result) shouldBe List("HTS-API015-005", "HTS-API015-012")
      correlationIds(result) shouldBe "correlationId"
    }
  }
}
